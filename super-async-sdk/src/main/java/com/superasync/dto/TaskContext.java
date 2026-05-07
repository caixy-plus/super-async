/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.dto;

public class TaskContext {
    private Long taskId;
    private String taskType;
    private String taskKey;
    private String payload;
    private int retryCount;
    private int maxRetry;

    TaskContext(Long taskId, String taskType, String taskKey, String payload, int retryCount, int maxRetry) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.taskKey = taskKey;
        this.payload = payload;
        this.retryCount = retryCount;
        this.maxRetry = maxRetry;
    }

    public static TaskContextBuilder builder() {
        return new TaskContextBuilder();
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

    public String getPayload() {
        return this.payload;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public int getMaxRetry() {
        return this.maxRetry;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TaskContext)) {
            return false;
        }
        TaskContext other = (TaskContext)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getRetryCount() != other.getRetryCount()) {
            return false;
        }
        if (this.getMaxRetry() != other.getMaxRetry()) {
            return false;
        }
        Long this$taskId = this.getTaskId();
        Long other$taskId = other.getTaskId();
        if (this$taskId == null ? other$taskId != null : !((Object)this$taskId).equals(other$taskId)) {
            return false;
        }
        String this$taskType = this.getTaskType();
        String other$taskType = other.getTaskType();
        if (this$taskType == null ? other$taskType != null : !this$taskType.equals(other$taskType)) {
            return false;
        }
        String this$taskKey = this.getTaskKey();
        String other$taskKey = other.getTaskKey();
        if (this$taskKey == null ? other$taskKey != null : !this$taskKey.equals(other$taskKey)) {
            return false;
        }
        String this$payload = this.getPayload();
        String other$payload = other.getPayload();
        return !(this$payload == null ? other$payload != null : !this$payload.equals(other$payload));
    }

    protected boolean canEqual(Object other) {
        return other instanceof TaskContext;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getRetryCount();
        result = result * 59 + this.getMaxRetry();
        Long $taskId = this.getTaskId();
        result = result * 59 + ($taskId == null ? 43 : ((Object)$taskId).hashCode());
        String $taskType = this.getTaskType();
        result = result * 59 + ($taskType == null ? 43 : $taskType.hashCode());
        String $taskKey = this.getTaskKey();
        result = result * 59 + ($taskKey == null ? 43 : $taskKey.hashCode());
        String $payload = this.getPayload();
        result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
        return result;
    }

    public String toString() {
        return "TaskContext(taskId=" + this.getTaskId() + ", taskType=" + this.getTaskType() + ", taskKey=" + this.getTaskKey() + ", payload=" + this.getPayload() + ", retryCount=" + this.getRetryCount() + ", maxRetry=" + this.getMaxRetry() + ")";
    }

    public static class TaskContextBuilder {
        private Long taskId;
        private String taskType;
        private String taskKey;
        private String payload;
        private int retryCount;
        private int maxRetry;

        TaskContextBuilder() {
        }

        public TaskContextBuilder taskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public TaskContextBuilder taskType(String taskType) {
            this.taskType = taskType;
            return this;
        }

        public TaskContextBuilder taskKey(String taskKey) {
            this.taskKey = taskKey;
            return this;
        }

        public TaskContextBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public TaskContextBuilder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public TaskContextBuilder maxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
            return this;
        }

        public TaskContext build() {
            return new TaskContext(this.taskId, this.taskType, this.taskKey, this.payload, this.retryCount, this.maxRetry);
        }

        public String toString() {
            return "TaskContext.TaskContextBuilder(taskId=" + this.taskId + ", taskType=" + this.taskType + ", taskKey=" + this.taskKey + ", payload=" + this.payload + ", retryCount=" + this.retryCount + ", maxRetry=" + this.maxRetry + ")";
        }
    }
}

