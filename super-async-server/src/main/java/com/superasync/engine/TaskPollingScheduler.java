/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Component
 *  org.springframework.transaction.annotation.Transactional
 */
package com.superasync.engine;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskStatus;
import com.superasync.engine.TaskExecutorEngine;
import com.superasync.engine.TaskRetryEngine;
import com.superasync.entity.AsyncTaskEntity;
import com.superasync.repository.AsyncTaskRepository;
import com.superasync.service.impl.TaskDispatcherImpl;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TaskPollingScheduler {
    private static final Logger log = LoggerFactory.getLogger(TaskPollingScheduler.class);
    private final AsyncTaskRepository taskRepository;
    private final TaskDispatcherImpl dispatcher;
    private final TaskExecutorEngine executorEngine;
    private final TaskRetryEngine retryEngine;

    @Scheduled(fixedDelayString = "${superasync.scheduler.poll-interval-ms:5000}")
    @Transactional
    public void pollAndDispatch() {
        List<AsyncTaskEntity> tasks = this.taskRepository.pollLocalTasks(OffsetDateTime.now(), 50);
        if (tasks.isEmpty()) {
            return;
        }
        log.info("[Scheduler] Polled {} pending tasks", (Object)tasks.size());
        for (AsyncTaskEntity task : tasks) {
            try {
                if (!this.dispatcher.hasExecutor(task.getTaskType())) {
                    log.warn("[Scheduler] No executor registered for taskType={}, taskId={}", (Object)task.getTaskType(), (Object)task.getId());
                    this.taskRepository.completeTask(task.getId(), TaskStatus.FAIL.name(), null, "No executor registered for type: " + task.getTaskType());
                    continue;
                }
                int locked = this.taskRepository.lockTask(task.getId(), this.getWorkerNode());
                if (locked == 0) {
                    log.debug("[Scheduler] Failed to lock task id={}", (Object)task.getId());
                    continue;
                }
                TaskContext context = this.buildContext(task);
                this.executorEngine.submit(task.getId(), context, this.dispatcher.getExecutor(task.getTaskType()));
            }
            catch (Exception e) {
                log.error("[Scheduler] Failed to dispatch task id={}", (Object)task.getId(), (Object)e);
                this.retryEngine.scheduleRetry(task);
            }
        }
    }

    private TaskContext buildContext(AsyncTaskEntity task) {
        return TaskContext.builder().taskId(task.getId()).taskType(task.getTaskType()).taskKey(task.getTaskKey()).payload(task.getPayload()).retryCount(task.getRetryCount()).maxRetry(task.getMaxRetry()).build();
    }

    private String getWorkerNode() {
        return System.getProperty("superasync.node", "default-node");
    }

    public TaskPollingScheduler(AsyncTaskRepository taskRepository, TaskDispatcherImpl dispatcher, TaskExecutorEngine executorEngine, TaskRetryEngine retryEngine) {
        this.taskRepository = taskRepository;
        this.dispatcher = dispatcher;
        this.executorEngine = executorEngine;
        this.retryEngine = retryEngine;
    }
}

