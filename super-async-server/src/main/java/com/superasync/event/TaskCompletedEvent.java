/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.ApplicationEvent
 */
package com.superasync.event;

import org.springframework.context.ApplicationEvent;

public class TaskCompletedEvent
extends ApplicationEvent {
    private final Long taskId;
    private final String taskType;
    private final String taskKey;
    private final boolean success;
    private final String resultPayload;
    private final String errorMsg;

    public TaskCompletedEvent(Object source, Long taskId, String taskType, String taskKey, boolean success, String resultPayload, String errorMsg) {
        super(source);
        this.taskId = taskId;
        this.taskType = taskType;
        this.taskKey = taskKey;
        this.success = success;
        this.resultPayload = resultPayload;
        this.errorMsg = errorMsg;
    }

    public Long getTaskId() {
        return this.taskId;
    }

    public String getTaskType() {
        return this.taskType;
    }

    public String getTaskKey() {
        return this.taskKey;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getResultPayload() {
        return this.resultPayload;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }
}

