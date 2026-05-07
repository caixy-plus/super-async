/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.service;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;

public interface TaskReceiptHandler {
    public void onSuccess(TaskContext var1, TaskResult var2);

    default public void onFailure(TaskContext context, TaskResult result) {
    }
}

