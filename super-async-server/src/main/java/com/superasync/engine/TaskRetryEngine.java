/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Component
 */
package com.superasync.engine;

import com.superasync.entity.AsyncTaskEntity;
import com.superasync.repository.AsyncTaskRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskRetryEngine {
    private static final Logger log = LoggerFactory.getLogger(TaskRetryEngine.class);
    private final AsyncTaskRepository taskRepository;
    private static final Duration BASE_DELAY = Duration.ofSeconds(10L);
    private static final Duration MAX_DELAY = Duration.ofMinutes(30L);

    public void scheduleRetry(AsyncTaskEntity task) {
        int retryCount = task.getRetryCount();
        Duration nextDelay = this.calculateDelay(retryCount);
        OffsetDateTime nextExecuteAt = OffsetDateTime.now().plus(nextDelay);
        this.taskRepository.markForRetry(task.getId(), nextExecuteAt);
        log.info("[RetryEngine] Scheduled retry for task id={}, retryCount={}, nextDelay={}s", new Object[]{task.getId(), retryCount + 1, nextDelay.getSeconds()});
    }

    private Duration calculateDelay(int retryCount) {
        long delayMillis = BASE_DELAY.toMillis() * (1L << retryCount);
        Duration delay = Duration.ofMillis(delayMillis);
        return delay.compareTo(MAX_DELAY) > 0 ? MAX_DELAY : delay;
    }

    public TaskRetryEngine(AsyncTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}

