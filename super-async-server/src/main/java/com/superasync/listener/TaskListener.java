/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.listener;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;

public interface TaskListener {
    public TaskResult onExecute(TaskContext var1);

    default public void onReceipt(TaskContext context, TaskResult result) {
    }
}

