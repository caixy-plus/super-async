package com.superasync.worker.logging;

import com.superasync.dto.TaskContext;

public class SuperAsyncWorkerLoggingContext {
    private static final ThreadLocal<TaskContext> CONTEXT = new ThreadLocal<>();

    public static void set(TaskContext context) {
        CONTEXT.set(context);
    }

    public static TaskContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
