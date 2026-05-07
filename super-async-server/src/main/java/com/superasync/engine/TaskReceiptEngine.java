/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Component
 */
package com.superasync.engine;

import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.service.TaskReceiptHandler;
import com.superasync.service.impl.TaskDispatcherImpl;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskReceiptEngine {
    private static final Logger log = LoggerFactory.getLogger(TaskReceiptEngine.class);
    private final TaskDispatcherImpl dispatcher;
    private final ExecutorService receiptExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
        Thread t = new Thread(r);
        t.setName("super-async-receipt-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    public void fireSuccess(TaskContext context, TaskResult result) {
        TaskReceiptHandler handler = this.dispatcher.getReceipt(context.getTaskType());
        if (handler == null) {
            log.debug("[ReceiptEngine] No receipt handler for taskType={}, skipping", (Object)context.getTaskType());
            return;
        }
        this.receiptExecutor.submit(() -> {
            try {
                handler.onSuccess(context, result);
                log.info("[ReceiptEngine] Success receipt handled for taskId={}", (Object)context.getTaskId());
            }
            catch (Exception e) {
                log.error("[ReceiptEngine] Success receipt failed for taskId={}", (Object)context.getTaskId(), (Object)e);
            }
        });
    }

    public void fireFailure(TaskContext context, TaskResult result) {
        TaskReceiptHandler handler = this.dispatcher.getReceipt(context.getTaskType());
        if (handler == null) {
            log.debug("[ReceiptEngine] No receipt handler for taskType={}, skipping", (Object)context.getTaskType());
            return;
        }
        this.receiptExecutor.submit(() -> {
            try {
                handler.onFailure(context, result);
                log.info("[ReceiptEngine] Failure receipt handled for taskId={}", (Object)context.getTaskId());
            }
            catch (Exception e) {
                log.error("[ReceiptEngine] Failure receipt failed for taskId={}", (Object)context.getTaskId(), (Object)e);
            }
        });
    }

    public TaskReceiptEngine(TaskDispatcherImpl dispatcher) {
        this.dispatcher = dispatcher;
    }
}

