package com.superasync.worker.engine;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.worker.SuperAsyncWorkerClient;
import com.superasync.worker.SuperAsyncWorkerProperties;
import com.superasync.worker.registry.SuperAsyncWorkerRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        log.info("[WorkerEngine] Started with corePoolSize={}, tags={}",
                properties.getCorePoolSize(), properties.getTags());
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

        try {
            if (!registry.hasHandler(task.getTaskType())) {
                log.error("[WorkerEngine] No handler for taskType={}, taskId={}", task.getTaskType(), task.getTaskId());
                client.complete(task.getTaskId(), executionId, false, null,
                        "No handler registered for type: " + task.getTaskType());
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
        }
    }
}
