/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.ApplicationEvent
 *  org.springframework.context.ApplicationEventPublisher
 *  org.springframework.stereotype.Component
 */
package com.superasync.engine;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.dto.TaskStatus;
import com.superasync.engine.TaskReceiptEngine;
import com.superasync.engine.TaskRetryEngine;
import com.superasync.entity.AsyncTaskEntity;
import com.superasync.event.TaskCompletedEvent;
import com.superasync.repository.AsyncTaskRepository;
import com.superasync.service.TaskExecutor;
import com.superasync.service.impl.TaskDispatcherImpl;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class TaskExecutorEngine {
    private static final Logger log = LoggerFactory.getLogger(TaskExecutorEngine.class);
    private final AsyncTaskRepository taskRepository;
    private final TaskDispatcherImpl dispatcher;
    private final TaskRetryEngine retryEngine;
    private final TaskReceiptEngine receiptEngine;
    private final ApplicationEventPublisher eventPublisher;
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
        log.info("[Executor] Executing task id={}, type={}, retry={}", new Object[]{taskId, context.getTaskType(), context.getRetryCount()});
        try {
            TaskResult result = executor.execute(context);
            if (result == null) {
                result = TaskResult.ok(null);
            }
            if (result.isSuccess()) {
                this.taskRepository.completeTask(taskId, TaskStatus.SUCCESS.name(), result.getPayload(), null);
                log.info("[Executor] Task id={} succeeded", (Object)taskId);
                this.receiptEngine.fireSuccess(context, result);
                this.publishEvent(taskId, context, true, result.getPayload(), null);
            } else {
                log.warn("[Executor] Task id={} returned failure: {}", (Object)taskId, (Object)result.getErrorMsg());
                this.handleFailure(taskId, context, result);
            }
        }
        catch (Exception e) {
            log.error("[Executor] Task id={} threw exception", (Object)taskId, (Object)e);
            TaskResult result = TaskResult.fail(e.getMessage());
            this.handleFailure(taskId, context, result);
        }
    }

    private void handleFailure(Long taskId, TaskContext context, TaskResult result) {
        AsyncTaskEntity task = this.taskRepository.findByTaskId(taskId);
        if (task != null && task.getRetryCount() < task.getMaxRetry()) {
            this.retryEngine.scheduleRetry(task);
        } else {
            this.taskRepository.completeTask(taskId, TaskStatus.FAIL.name(), result.getPayload(), result.getErrorMsg());
            log.error("[Executor] Task id={} failed after all retries", (Object)taskId);
            this.receiptEngine.fireFailure(context, result);
            this.publishEvent(taskId, context, false, result.getPayload(), result.getErrorMsg());
        }
    }

    private void publishEvent(Long taskId, TaskContext context, boolean success, String payload, String errorMsg) {
        try {
            this.eventPublisher.publishEvent((ApplicationEvent)new TaskCompletedEvent(this, taskId, context.getTaskType(), context.getTaskKey(), success, payload, errorMsg));
        }
        catch (Exception e) {
            log.error("[Executor] Failed to publish event for taskId={}", (Object)taskId, (Object)e);
        }
    }

    public TaskExecutorEngine(AsyncTaskRepository taskRepository, TaskDispatcherImpl dispatcher, TaskRetryEngine retryEngine, TaskReceiptEngine receiptEngine, ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.dispatcher = dispatcher;
        this.retryEngine = retryEngine;
        this.receiptEngine = receiptEngine;
        this.eventPublisher = eventPublisher;
    }
}

