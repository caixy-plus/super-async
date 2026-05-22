/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.superasync.service.impl;

import com.superasync.dto.TaskRequest;
import com.superasync.dto.TaskStatus;
import com.superasync.entity.AsyncTaskEntity;
import com.superasync.repository.AsyncTaskRepository;
import com.superasync.service.TaskDispatcher;
import com.superasync.event.TaskSubmittedEvent;
import com.superasync.service.TaskExecutor;
import com.superasync.service.TaskReceiptHandler;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskDispatcherImpl
implements TaskDispatcher {
    private static final Logger log = LoggerFactory.getLogger(TaskDispatcherImpl.class);
    private final AsyncTaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, TaskExecutor> executors = new ConcurrentHashMap<String, TaskExecutor>();
    private final Map<String, TaskReceiptHandler> receipts = new ConcurrentHashMap<String, TaskReceiptHandler>();

    @Override
    @Transactional
    public Long submit(TaskRequest request) {
        AsyncTaskEntity existing = this.taskRepository.findByTaskKey(request.getTaskKey());
        if (existing != null && !isTerminalStatus(existing.getStatus())) {
            log.info("[TaskDispatcher] Idempotent hit, taskId={} key={}", (Object)existing.getId(), (Object)request.getTaskKey());
            return existing.getId();
        }
        if (existing != null && isTerminalStatus(existing.getStatus())) {
            existing.setStatus(TaskStatus.PENDING.name());
            existing.setPayload(request.getPayload());
            existing.setPriority(request.getPriority().value());
            existing.setMaxRetry(request.getMaxRetry());
            existing.setWorkerTag(request.getWorkerTag());
            existing.setScheduledJobId(request.getScheduledJobId());
            existing.setExecutionId(request.getExecutionId());
            existing.setExecuteAt(OffsetDateTime.now().plus(request.getDelay()));
            existing.setTimeoutAt(OffsetDateTime.now().plus(request.getDelay()).plus(request.getTimeout()));
            existing.setRetryCount(0);
            existing.setResultPayload(null);
            existing.setErrorMsg(null);
            existing.setWorkerNode(null);
            existing.setUpdatedAt(OffsetDateTime.now());
            this.taskRepository.save(existing);
            log.info("[TaskDispatcher] Resurrected terminal task id={}, type={}, key={}",
                existing.getId(), existing.getTaskType(), existing.getTaskKey());
            this.eventPublisher.publishEvent(new TaskSubmittedEvent(this, existing.getId()));
            return existing.getId();
        }
        AsyncTaskEntity task = new AsyncTaskEntity();
        task.setTaskType(request.getTaskType());
        task.setTaskKey(request.getTaskKey());
        task.setPayload(request.getPayload());
        task.setPriority(request.getPriority().value());
        task.setStatus(TaskStatus.PENDING.name());
        task.setMaxRetry(request.getMaxRetry());
        task.setWorkerTag(request.getWorkerTag());
        task.setScheduledJobId(request.getScheduledJobId());
        task.setExecutionId(request.getExecutionId());
        task.setExecuteAt(OffsetDateTime.now().plus(request.getDelay()));
        task.setTimeoutAt(OffsetDateTime.now().plus(request.getDelay()).plus(request.getTimeout()));
        this.taskRepository.save(task);
        log.info("[TaskDispatcher] Submitted task id={}, type={}, key={}", new Object[]{task.getId(), task.getTaskType(), task.getTaskKey()});
        this.eventPublisher.publishEvent(new TaskSubmittedEvent(this, task.getId()));
        return task.getId();
    }

    @Override
    public void registerExecutor(String taskType, TaskExecutor executor) {
        this.executors.put(taskType, executor);
        log.info("[TaskDispatcher] Registered executor for taskType={}", (Object)taskType);
    }

    @Override
    public void registerReceipt(String taskType, TaskReceiptHandler receiptHandler) {
        this.receipts.put(taskType, receiptHandler);
        log.info("[TaskDispatcher] Registered receipt handler for taskType={}", (Object)taskType);
    }

    public TaskExecutor getExecutor(String taskType) {
        return this.executors.get(taskType);
    }

    public TaskReceiptHandler getReceipt(String taskType) {
        return this.receipts.get(taskType);
    }

    public boolean hasExecutor(String taskType) {
        return this.executors.containsKey(taskType);
    }

    private boolean isTerminalStatus(String status) {
        if (status == null) {
            return false;
        }
        String upper = status.toUpperCase();
        return "SUCCESS".equals(upper) || "FAIL".equals(upper) || "TIMEOUT".equals(upper);
    }

    public TaskDispatcherImpl(AsyncTaskRepository taskRepository, ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }
}

