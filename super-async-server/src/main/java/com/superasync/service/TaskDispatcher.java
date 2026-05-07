/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.service;

import com.superasync.dto.TaskRequest;
import com.superasync.service.TaskExecutor;
import com.superasync.service.TaskReceiptHandler;

public interface TaskDispatcher {
    public Long submit(TaskRequest var1);

    public void registerExecutor(String var1, TaskExecutor var2);

    public void registerReceipt(String var1, TaskReceiptHandler var2);
}

