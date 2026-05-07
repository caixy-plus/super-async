/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.dto;

public class TaskResult {
    private boolean success;
    private String payload;
    private String errorMsg;

    public static TaskResult ok(String payload) {
        return TaskResult.builder().success(true).payload(payload).build();
    }

    public static TaskResult fail(String errorMsg) {
        return TaskResult.builder().success(false).errorMsg(errorMsg).build();
    }

    TaskResult(boolean success, String payload, String errorMsg) {
        this.success = success;
        this.payload = payload;
        this.errorMsg = errorMsg;
    }

    public static TaskResultBuilder builder() {
        return new TaskResultBuilder();
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getPayload() {
        return this.payload;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TaskResult)) {
            return false;
        }
        TaskResult other = (TaskResult)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isSuccess() != other.isSuccess()) {
            return false;
        }
        String this$payload = this.getPayload();
        String other$payload = other.getPayload();
        if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload)) {
            return false;
        }
        String this$errorMsg = this.getErrorMsg();
        String other$errorMsg = other.getErrorMsg();
        return !(this$errorMsg == null ? other$errorMsg != null : !this$errorMsg.equals(other$errorMsg));
    }

    protected boolean canEqual(Object other) {
        return other instanceof TaskResult;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isSuccess() ? 79 : 97);
        String $payload = this.getPayload();
        result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
        String $errorMsg = this.getErrorMsg();
        result = result * 59 + ($errorMsg == null ? 43 : $errorMsg.hashCode());
        return result;
    }

    public String toString() {
        return "TaskResult(success=" + this.isSuccess() + ", payload=" + this.getPayload() + ", errorMsg=" + this.getErrorMsg() + ")";
    }

    public static class TaskResultBuilder {
        private boolean success;
        private String payload;
        private String errorMsg;

        TaskResultBuilder() {
        }

        public TaskResultBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public TaskResultBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public TaskResultBuilder errorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
            return this;
        }

        public TaskResult build() {
            return new TaskResult(this.success, this.payload, this.errorMsg);
        }

        public String toString() {
            return "TaskResult.TaskResultBuilder(success=" + this.success + ", payload=" + this.payload + ", errorMsg=" + this.errorMsg + ")";
        }
    }
}

