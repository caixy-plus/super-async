package com.superasync.worker.engine;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.worker.SuperAsyncWorkerClient;
import com.superasync.worker.SuperAsyncWorkerProperties;
import com.superasync.worker.logging.LogEntry;
import com.superasync.worker.logging.SuperAsyncWorkerLoggingContext;
import com.superasync.worker.registry.SuperAsyncWorkerRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Worker 执行引擎
 * <p>定期从调度器拉取任务，在线程池中执行，并上报结果。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "superasync.worker", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SuperAsyncWorkerEngine {

    private final SuperAsyncWorkerClient client;
    private final SuperAsyncWorkerRegistry registry;
    private final SuperAsyncWorkerProperties properties;

    private ExecutorService executorService;
    private BlockingQueue<LogEntry> logBuffer;
    private ScheduledExecutorService logFlushExecutor;
    private final Object flushLock = new Object();

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(
                properties.getCorePoolSize(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("super-async-worker-" + t.getId());
                    t.setDaemon(true);
                    return t;
                }
        );
        logBuffer = new LinkedBlockingQueue<>(10000);
        logFlushExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("super-async-log-flusher");
            t.setDaemon(true);
            return t;
        });
        logFlushExecutor.scheduleAtFixedRate(this::flushLogs, 1, 1, TimeUnit.SECONDS);

        // 动态加载 polling tags：注解 taskType + 定时任务 workerTag
        loadDynamicTags();

        log.info("[WorkerEngine] Started with corePoolSize={}, tags={}",
                properties.getCorePoolSize(), properties.getTags());
    }

    private void loadDynamicTags() {
        List<String> tags = new ArrayList<>();
        if (properties.getTags() != null && !properties.getTags().isEmpty()) {
            tags.addAll(properties.getTags());
        }
        // 1. 扫描 @SuperAsyncWorker 注解的 taskType
        tags.addAll(registry.getHandlers().keySet());
        // 2. 从调度器获取所有定时任务的 workerTag（重试 5 次，仍失败则启动失败触发 K8s 重启）
        List<String> scheduledTags = null;
        Exception lastError = null;
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                scheduledTags = client.fetchScheduledJobTags();
                break;
            } catch (Exception e) {
                lastError = e;
                log.warn("[WorkerEngine] Fetch scheduled job tags failed (attempt {}/5), retrying in 3s...", attempt, e);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("[WorkerEngine] Interrupted while fetching scheduled job tags", ie);
                }
            }
        }
        if (scheduledTags == null) {
            throw new IllegalStateException("[WorkerEngine] Failed to fetch scheduled job tags after 5 attempts, cannot start", lastError);
        }
        tags.addAll(scheduledTags);
        List<String> uniqueTags = tags.stream().filter(t -> t != null && !t.isBlank()).distinct().toList();
        properties.setTags(uniqueTags);
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if (logFlushExecutor != null) {
            logFlushExecutor.shutdown();
            try {
                if (!logFlushExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logFlushExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logFlushExecutor.shutdownNow();
            }
        }
    }

    @Scheduled(fixedDelayString = "${superasync.worker.poll-interval-ms:3000}")
    public void pollAndExecute() {
        if (executorService == null || executorService.isShutdown()) {
            return;
        }

        List<SuperAsyncWorkerClient.WorkerTask> tasks = client.pollBatch(10);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        for (SuperAsyncWorkerClient.WorkerTask task : tasks) {
            executorService.submit(() -> execute(task));
        }
    }

    private void flushLogs() {
        synchronized (flushLock) {
            List<LogEntry> batch = new ArrayList<>();
            logBuffer.drainTo(batch);
            if (batch.isEmpty()) {
                return;
            }
            try {
                client.appendLogBatch(batch);
            } catch (Exception e) {
                log.error("[WorkerEngine] Flush logs failed", e);
            }
        }
    }

    private void execute(SuperAsyncWorkerClient.WorkerTask task) {
        log.info("[WorkerEngine] Executing task id={}, type={}, retry={}",
                task.getTaskId(), task.getTaskType(), task.getRetryCount());

        Long executionId = task.getExecutionId();

        TaskContext context = TaskContext.builder()
                .taskId(task.getTaskId())
                .taskType(task.getTaskType())
                .taskKey(task.getTaskKey())
                .payload(task.getPayload())
                .retryCount(task.getRetryCount())
                .maxRetry(task.getMaxRetry())
                .executionId(executionId)
                .build();

        final Long execId = executionId;
        context.setLogAppender((level, msg) -> {
            if (logBuffer != null) {
                logBuffer.offer(new LogEntry(execId, level, msg, System.currentTimeMillis()));
            }
        });
        context.log("INFO", String.format("[Worker] Task id=%d started (type=%s)", task.getTaskId(), task.getTaskType()));

        try {
            SuperAsyncWorkerLoggingContext.set(context);

            if (!registry.hasHandler(task.getTaskType())) {
                String errMsg = "No handler registered for type: " + task.getTaskType();
                log.error("[WorkerEngine] No handler for taskType={}, taskId={}", task.getTaskType(), task.getTaskId());
                client.complete(task.getTaskId(), executionId, false, null, errMsg);
                return;
            }

            TaskResult result = registry.execute(task.getTaskType(), context);
            if (result == null) {
                result = TaskResult.ok(null);
            }

            if (result.isSuccess()) {
                client.complete(task.getTaskId(), executionId, true, result.getPayload(), null);
                log.info("[WorkerEngine] Task id={} succeeded", task.getTaskId());
            } else {
                client.complete(task.getTaskId(), executionId, false, result.getPayload(), result.getErrorMsg());
                log.warn("[WorkerEngine] Task id={} failed: {}", task.getTaskId(), result.getErrorMsg());
            }
        } catch (Exception e) {
            log.error("[WorkerEngine] Task id={} threw exception", task.getTaskId(), e);
            client.complete(task.getTaskId(), executionId, false, null, e.getMessage());
        } finally {
            SuperAsyncWorkerLoggingContext.clear();
            flushLogs();
        }
    }
}
