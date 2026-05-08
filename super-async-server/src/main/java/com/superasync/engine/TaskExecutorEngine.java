package com.superasync.engine;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.dto.TaskStatus;
import com.superasync.entity.AsyncTaskEntity;
import com.superasync.event.TaskCompletedEvent;
import com.superasync.repository.AsyncTaskRepository;
import com.superasync.service.JobExecutionService;
import com.superasync.service.TaskExecutor;
import com.superasync.service.impl.TaskDispatcherImpl;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TaskExecutorEngine {
    private static final Logger log = LoggerFactory.getLogger(TaskExecutorEngine.class);
    private final AsyncTaskRepository taskRepository;
    private final TaskDispatcherImpl dispatcher;
    private final TaskRetryEngine retryEngine;
    private final TaskReceiptEngine receiptEngine;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final JobExecutionService executionService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, r -> {
        Thread t = new Thread(r);
        t.setName("super-async-exec-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    public void submit(Long taskId, TaskContext context, TaskExecutor executor) {
        this.executorService.submit(() -> this.execute(taskId, context, executor));
    }

    private void execute(Long taskId, TaskContext context, TaskExecutor executor) {
        log.info("[Executor] Executing task id={}, type={}, retry={}", taskId, context.getTaskType(), context.getRetryCount());
        Long executionId = context.getExecutionId();
        if (executionId != null) {
            executionService.markProcessing(executionId);
            executionService.appendLog(executionId, "INFO", String.format("[Executor] Start task id=%d type=%s retry=%d", taskId, context.getTaskType(), context.getRetryCount()));
        }
        try {
            TaskResult result = executor.execute(context);
            if (result == null) {
                result = TaskResult.ok(null);
            }
            final TaskResult finalResult = result;
            if (finalResult.isSuccess()) {
                this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        taskRepository.completeTask(taskId, TaskStatus.SUCCESS.name(), finalResult.getPayload(), null);
                    }
                });
                log.info("[Executor] Task id={} succeeded", taskId);
                if (executionId != null) {
                    executionService.markCompleted(executionId, true, null);
                    executionService.appendLog(executionId, "INFO", String.format("[Executor] Task id=%d succeeded", taskId));
                }
                this.receiptEngine.fireSuccess(context, finalResult);
                this.publishEvent(taskId, context, true, finalResult.getPayload(), null);
            } else {
                log.warn("[Executor] Task id={} returned failure: {}", taskId, finalResult.getErrorMsg());
                if (executionId != null) {
                    executionService.markCompleted(executionId, false, finalResult.getErrorMsg());
                    executionService.appendLog(executionId, "ERROR", String.format("[Executor] Task id=%d failed: %s", taskId, finalResult.getErrorMsg()));
                }
                this.handleFailure(taskId, context, finalResult);
            }
        }
        catch (Exception e) {
            log.error("[Executor] Task id={} threw exception", taskId, e);
            if (executionId != null) {
                executionService.markCompleted(executionId, false, e.getMessage());
                executionService.appendLog(executionId, "ERROR", String.format("[Executor] Task id=%d threw exception: %s", taskId, e.getMessage()));
            }
            TaskResult result = TaskResult.fail(e.getMessage());
            this.handleFailure(taskId, context, result);
        }
    }

    private void handleFailure(Long taskId, TaskContext context, TaskResult result) {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncTaskEntity task = taskRepository.findByTaskId(taskId);
                if (task != null && task.getRetryCount() < task.getMaxRetry()) {
                    retryEngine.scheduleRetry(task);
                } else {
                    taskRepository.completeTask(taskId, TaskStatus.FAIL.name(), result.getPayload(), result.getErrorMsg());
                    log.error("[Executor] Task id={} failed after all retries", taskId);
                    receiptEngine.fireFailure(context, result);
                    publishEvent(taskId, context, false, result.getPayload(), result.getErrorMsg());
                }
            }
        });
    }

    private void publishEvent(Long taskId, TaskContext context, boolean success, String payload, String errorMsg) {
        try {
            this.eventPublisher.publishEvent((ApplicationEvent)new TaskCompletedEvent(this, taskId, context.getTaskType(), context.getTaskKey(), success, payload, errorMsg));
        }
        catch (Exception e) {
            log.error("[Executor] Failed to publish event for taskId={}", taskId, e);
        }
    }

    public TaskExecutorEngine(AsyncTaskRepository taskRepository, TaskDispatcherImpl dispatcher, TaskRetryEngine retryEngine, TaskReceiptEngine receiptEngine, ApplicationEventPublisher eventPublisher, PlatformTransactionManager transactionManager, JobExecutionService executionService) {
        this.taskRepository = taskRepository;
        this.dispatcher = dispatcher;
        this.retryEngine = retryEngine;
        this.receiptEngine = receiptEngine;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.executionService = executionService;
    }
}
