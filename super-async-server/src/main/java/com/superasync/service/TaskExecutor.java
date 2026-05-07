/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.service;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;

public interface TaskExecutor {
    public TaskResult execute(TaskContext var1);
}

