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

import com.superasync.dto.TaskStatus;
import com.superasync.engine.TaskRetryEngine;
import com.superasync.entity.AsyncTaskEntity;
import com.superasync.repository.AsyncTaskRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TaskTimeoutWatch {
    private static final Logger log = LoggerFactory.getLogger(TaskTimeoutWatch.class);
    private final AsyncTaskRepository taskRepository;
    private final TaskRetryEngine retryEngine;

    @Scheduled(fixedDelay=60000L)
    @Transactional
    public void checkTimeout() {
        List<AsyncTaskEntity> tasks = this.taskRepository.pollTimeoutTasks(OffsetDateTime.now(), 50);
        if (tasks.isEmpty()) {
            return;
        }
        log.info("[TimeoutWatch] Found {} timeout tasks", (Object)tasks.size());
        for (AsyncTaskEntity task : tasks) {
            try {
                log.warn("[TimeoutWatch] Task id={} timed out, retryCount={}/maxRetry={}", new Object[]{task.getId(), task.getRetryCount(), task.getMaxRetry()});
                if (task.getRetryCount() < task.getMaxRetry()) {
                    this.retryEngine.scheduleRetry(task);
                    continue;
                }
                this.taskRepository.completeTask(task.getId(), TaskStatus.TIMEOUT.name(), null, "Task timed out after " + task.getRetryCount() + " retries");
            }
            catch (Exception e) {
                log.error("[TimeoutWatch] Failed to handle timeout task id={}", (Object)task.getId(), (Object)e);
            }
        }
    }

    public TaskTimeoutWatch(AsyncTaskRepository taskRepository, TaskRetryEngine retryEngine) {
        this.taskRepository = taskRepository;
        this.retryEngine = retryEngine;
    }
}

