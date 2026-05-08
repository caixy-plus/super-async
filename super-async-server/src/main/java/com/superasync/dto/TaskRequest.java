/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.dto;

import com.superasync.dto.Priority;
import java.time.Duration;

public class TaskRequest {
    private String taskType;
    private String taskKey;
    private String payload;
    private Priority priority;
    private Duration delay;
    private Duration timeout;
    private int maxRetry;
    private String workerTag;
    private Long scheduledJobId;
    private Long executionId;

    private static Priority $default$priority() {
        return Priority.NORMAL;
    }

    private static Duration $default$delay() {
        return Duration.ZERO;
    }

    private static Duration $default$timeout() {
        return Duration.ofMinutes(10L);
    }

    private static int $default$maxRetry() {
        return 3;
    }

    TaskRequest(String taskType, String taskKey, String payload, Priority priority, Duration delay, Duration timeout, int maxRetry, String workerTag, Long scheduledJobId, Long executionId) {
        this.taskType = taskType;
        this.taskKey = taskKey;
        this.payload = payload;
        this.priority = priority;
        this.delay = delay;
        this.timeout = timeout;
        this.maxRetry = maxRetry;
        this.workerTag = workerTag;
        this.scheduledJobId = scheduledJobId;
        this.executionId = executionId;
    }

    public static TaskRequestBuilder builder() {
        return new TaskRequestBuilder();
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

    public Priority getPriority() {
        return this.priority;
    }

    public Duration getDelay() {
        return this.delay;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    public int getMaxRetry() {
        return this.maxRetry;
    }

    public String getWorkerTag() {
        return this.workerTag;
    }

    public Long getScheduledJobId() {
        return this.scheduledJobId;
    }

    public Long getExecutionId() {
        return this.executionId;
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

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public void setWorkerTag(String workerTag) {
        this.workerTag = workerTag;
    }

    public void setScheduledJobId(Long scheduledJobId) {
        this.scheduledJobId = scheduledJobId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TaskRequest)) {
            return false;
        }
        TaskRequest other = (TaskRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getMaxRetry() != other.getMaxRetry()) {
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
        if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload)) {
            return false;
        }
        Priority this$priority = this.getPriority();
        Priority other$priority = other.getPriority();
        if (this$priority == null ? other$priority != null : !((Object)((Object)this$priority)).equals((Object)other$priority)) {
            return false;
        }
        Duration this$delay = this.getDelay();
        Duration other$delay = other.getDelay();
        if (this$delay == null ? other$delay != null : !((Object)this$delay).equals(other$delay)) {
            return false;
        }
        Duration this$timeout = this.getTimeout();
        Duration other$timeout = other.getTimeout();
        if (this$timeout == null ? other$timeout != null : !((Object)this$timeout).equals(other$timeout)) {
            return false;
        }
        String this$workerTag = this.getWorkerTag();
        String other$workerTag = other.getWorkerTag();
        if (this$workerTag == null ? other$workerTag != null : !this$workerTag.equals(other$workerTag)) {
            return false;
        }
        Long this$scheduledJobId = this.getScheduledJobId();
        Long other$scheduledJobId = other.getScheduledJobId();
        if (this$scheduledJobId == null ? other$scheduledJobId != null : !((Object)this$scheduledJobId).equals(other$scheduledJobId)) {
            return false;
        }
        Long this$executionId = this.getExecutionId();
        Long other$executionId = other.getExecutionId();
        return !(this$executionId == null ? other$executionId != null : !((Object)this$executionId).equals(other$executionId));
    }

    protected boolean canEqual(Object other) {
        return other instanceof TaskRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getMaxRetry();
        String $taskType = this.getTaskType();
        result = result * 59 + ($taskType == null ? 43 : $taskType.hashCode());
        String $taskKey = this.getTaskKey();
        result = result * 59 + ($taskKey == null ? 43 : $taskKey.hashCode());
        String $payload = this.getPayload();
        result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
        Priority $priority = this.getPriority();
        result = result * 59 + ($priority == null ? 43 : ((Object)((Object)$priority)).hashCode());
        Duration $delay = this.getDelay();
        result = result * 59 + ($delay == null ? 43 : ((Object)$delay).hashCode());
        Duration $timeout = this.getTimeout();
        result = result * 59 + ($timeout == null ? 43 : ((Object)$timeout).hashCode());
        String $workerTag = this.getWorkerTag();
        result = result * 59 + ($workerTag == null ? 43 : $workerTag.hashCode());
        Long $scheduledJobId = this.getScheduledJobId();
        result = result * 59 + ($scheduledJobId == null ? 43 : ((Object)$scheduledJobId).hashCode());
        Long $executionId = this.getExecutionId();
        result = result * 59 + ($executionId == null ? 43 : ((Object)$executionId).hashCode());
        return result;
    }

    public String toString() {
        return "TaskRequest(taskType=" + this.getTaskType() + ", taskKey=" + this.getTaskKey() + ", payload=" + this.getPayload() + ", priority=" + String.valueOf((Object)this.getPriority()) + ", delay=" + String.valueOf(this.getDelay()) + ", timeout=" + String.valueOf(this.getTimeout()) + ", maxRetry=" + this.getMaxRetry() + ", workerTag=" + this.getWorkerTag() + ", scheduledJobId=" + this.getScheduledJobId() + ", executionId=" + this.getExecutionId() + ")";
    }

    public static class TaskRequestBuilder {
        private String taskType;
        private String taskKey;
        private String payload;
        private boolean priority$set;
        private Priority priority$value;
        private boolean delay$set;
        private Duration delay$value;
        private boolean timeout$set;
        private Duration timeout$value;
        private boolean maxRetry$set;
        private int maxRetry$value;
        private String workerTag;
        private Long scheduledJobId;
        private Long executionId;

        TaskRequestBuilder() {
        }

        public TaskRequestBuilder taskType(String taskType) {
            this.taskType = taskType;
            return this;
        }

        public TaskRequestBuilder taskKey(String taskKey) {
            this.taskKey = taskKey;
            return this;
        }

        public TaskRequestBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public TaskRequestBuilder priority(Priority priority) {
            this.priority$value = priority;
            this.priority$set = true;
            return this;
        }

        public TaskRequestBuilder delay(Duration delay) {
            this.delay$value = delay;
            this.delay$set = true;
            return this;
        }

        public TaskRequestBuilder timeout(Duration timeout) {
            this.timeout$value = timeout;
            this.timeout$set = true;
            return this;
        }

        public TaskRequestBuilder maxRetry(int maxRetry) {
            this.maxRetry$value = maxRetry;
            this.maxRetry$set = true;
            return this;
        }

        public TaskRequestBuilder workerTag(String workerTag) {
            this.workerTag = workerTag;
            return this;
        }

        public TaskRequestBuilder scheduledJobId(Long scheduledJobId) {
            this.scheduledJobId = scheduledJobId;
            return this;
        }

        public TaskRequestBuilder executionId(Long executionId) {
            this.executionId = executionId;
            return this;
        }

        public TaskRequest build() {
            Priority priority$value = this.priority$value;
            if (!this.priority$set) {
                priority$value = TaskRequest.$default$priority();
            }
            Duration delay$value = this.delay$value;
            if (!this.delay$set) {
                delay$value = TaskRequest.$default$delay();
            }
            Duration timeout$value = this.timeout$value;
            if (!this.timeout$set) {
                timeout$value = TaskRequest.$default$timeout();
            }
            int maxRetry$value = this.maxRetry$value;
            if (!this.maxRetry$set) {
                maxRetry$value = TaskRequest.$default$maxRetry();
            }
            return new TaskRequest(this.taskType, this.taskKey, this.payload, priority$value, delay$value, timeout$value, maxRetry$value, this.workerTag, this.scheduledJobId, this.executionId);
        }

        public String toString() {
            return "TaskRequest.TaskRequestBuilder(taskType=" + this.taskType + ", taskKey=" + this.taskKey + ", payload=" + this.payload + ", priority$value=" + String.valueOf((Object)this.priority$value) + ", delay$value=" + String.valueOf(this.delay$value) + ", timeout$value=" + String.valueOf(this.timeout$value) + ", maxRetry$value=" + this.maxRetry$value + ", workerTag=" + this.workerTag + ", scheduledJobId=" + this.scheduledJobId + ", executionId=" + this.executionId + ")";
        }
    }
}
