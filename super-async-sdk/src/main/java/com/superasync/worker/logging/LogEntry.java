package com.superasync.worker.logging;

import lombok.Data;

@Data
public class LogEntry {
    private Long executionId;
    private String level;
    private String message;
    private long timestamp;

    public LogEntry(Long executionId, String level, String message, long timestamp) {
        this.executionId = executionId;
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
    }
}
