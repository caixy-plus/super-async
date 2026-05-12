package com.superasync.worker.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import com.superasync.dto.TaskContext;

public class SuperAsyncLogbackAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        try {
            TaskContext ctx = SuperAsyncWorkerLoggingContext.get();
            if (ctx == null) {
                return;
            }

            String level = event.getLevel().toString();
            String message = event.getFormattedMessage();

            IThrowableProxy tp = event.getThrowableProxy();
            if (tp != null) {
                message += "\n" + formatThrowable(tp);
            }

            ctx.log(level, message);
        } catch (Exception e) {
            // 静默丢弃，绝不影响主业务
        }
    }

    private static String formatThrowable(IThrowableProxy tp) {
        StringBuilder sb = new StringBuilder();
        formatThrowable(sb, tp);
        return sb.toString();
    }

    private static void formatThrowable(StringBuilder sb, IThrowableProxy tp) {
        if (tp == null) {
            return;
        }
        sb.append(tp.getClassName()).append(": ").append(tp.getMessage()).append("\n");
        StackTraceElementProxy[] steps = tp.getStackTraceElementProxyArray();
        if (steps != null) {
            for (StackTraceElementProxy step : steps) {
                sb.append("\tat ").append(step.getStackTraceElement().toString()).append("\n");
            }
        }
        IThrowableProxy cause = tp.getCause();
        if (cause != null) {
            sb.append("Caused by: ");
            formatThrowable(sb, cause);
        }
    }
}
