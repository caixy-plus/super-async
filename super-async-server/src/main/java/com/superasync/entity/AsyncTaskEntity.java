/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Table
 *  org.hibernate.annotations.CreationTimestamp
 *  org.hibernate.annotations.UpdateTimestamp
 */
package com.superasync.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="async_tasks")
public class AsyncTaskEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="task_type", nullable=false, length=64)
    private String taskType;
    @Column(name="task_key", nullable=false, unique=true, length=255)
    private String taskKey;
    @Column(name="payload", nullable=false, columnDefinition="text")
    private String payload;
    @Column(name="priority", nullable=false)
    private Integer priority = 5;
    @Column(name="status", nullable=false, length=20)
    private String status;
    @Column(name="retry_count", nullable=false)
    private Integer retryCount = 0;
    @Column(name="max_retry", nullable=false)
    private Integer maxRetry = 3;
    @Column(name="execute_at", nullable=false)
    private OffsetDateTime executeAt;
    @Column(name="timeout_at")
    private OffsetDateTime timeoutAt;
    @Column(name="result_payload", columnDefinition="text")
    private String resultPayload;
    @Column(name="error_msg")
    private String errorMsg;
    @Column(name="worker_node", length=64)
    private String workerNode;
    @Column(name="worker_tag", length=64)
    private String workerTag;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    public Long getId() {
        return this.id;
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

    public Integer getPriority() {
        return this.priority;
    }

    public String getStatus() {
        return this.status;
    }

    public Integer getRetryCount() {
        return this.retryCount;
    }

    public Integer getMaxRetry() {
        return this.maxRetry;
    }

    public OffsetDateTime getExecuteAt() {
        return this.executeAt;
    }

    public OffsetDateTime getTimeoutAt() {
        return this.timeoutAt;
    }

    public String getResultPayload() {
        return this.resultPayload;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public String getWorkerNode() {
        return this.workerNode;
    }

    public String getWorkerTag() {
        return this.workerTag;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void setMaxRetry(Integer maxRetry) {
        this.maxRetry = maxRetry;
    }

    public void setExecuteAt(OffsetDateTime executeAt) {
        this.executeAt = executeAt;
    }

    public void setTimeoutAt(OffsetDateTime timeoutAt) {
        this.timeoutAt = timeoutAt;
    }

    public void setResultPayload(String resultPayload) {
        this.resultPayload = resultPayload;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setWorkerNode(String workerNode) {
        this.workerNode = workerNode;
    }

    public void setWorkerTag(String workerTag) {
        this.workerTag = workerTag;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AsyncTaskEntity)) {
            return false;
        }
        AsyncTaskEntity other = (AsyncTaskEntity)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Integer this$priority = this.getPriority();
        Integer other$priority = other.getPriority();
        if (this$priority == null ? other$priority != null : !((Object)this$priority).equals(other$priority)) {
            return false;
        }
        Integer this$retryCount = this.getRetryCount();
        Integer other$retryCount = other.getRetryCount();
        if (this$retryCount == null ? other$retryCount != null : !((Object)this$retryCount).equals(other$retryCount)) {
            return false;
        }
        Integer this$maxRetry = this.getMaxRetry();
        Integer other$maxRetry = other.getMaxRetry();
        if (this$maxRetry == null ? other$maxRetry != null : !((Object)this$maxRetry).equals(other$maxRetry)) {
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
        String this$status = this.getStatus();
        String other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) {
            return false;
        }
        OffsetDateTime this$executeAt = this.getExecuteAt();
        OffsetDateTime other$executeAt = other.getExecuteAt();
        if (this$executeAt == null ? other$executeAt != null : !((Object)this$executeAt).equals(other$executeAt)) {
            return false;
        }
        OffsetDateTime this$timeoutAt = this.getTimeoutAt();
        OffsetDateTime other$timeoutAt = other.getTimeoutAt();
        if (this$timeoutAt == null ? other$timeoutAt != null : !((Object)this$timeoutAt).equals(other$timeoutAt)) {
            return false;
        }
        String this$resultPayload = this.getResultPayload();
        String other$resultPayload = other.getResultPayload();
        if (this$resultPayload == null ? other$resultPayload != null : !this$resultPayload.equals(other$resultPayload)) {
            return false;
        }
        String this$errorMsg = this.getErrorMsg();
        String other$errorMsg = other.getErrorMsg();
        if (this$errorMsg == null ? other$errorMsg != null : !this$errorMsg.equals(other$errorMsg)) {
            return false;
        }
        String this$workerNode = this.getWorkerNode();
        String other$workerNode = other.getWorkerNode();
        if (this$workerNode == null ? other$workerNode != null : !this$workerNode.equals(other$workerNode)) {
            return false;
        }
        String this$workerTag = this.getWorkerTag();
        String other$workerTag = other.getWorkerTag();
        if (this$workerTag == null ? other$workerTag != null : !this$workerTag.equals(other$workerTag)) {
            return false;
        }
        OffsetDateTime this$createdAt = this.getCreatedAt();
        OffsetDateTime other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !((Object)this$createdAt).equals(other$createdAt)) {
            return false;
        }
        OffsetDateTime this$updatedAt = this.getUpdatedAt();
        OffsetDateTime other$updatedAt = other.getUpdatedAt();
        return !(this$updatedAt == null ? other$updatedAt != null : !((Object)this$updatedAt).equals(other$updatedAt));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AsyncTaskEntity;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Integer $priority = this.getPriority();
        result = result * 59 + ($priority == null ? 43 : ((Object)$priority).hashCode());
        Integer $retryCount = this.getRetryCount();
        result = result * 59 + ($retryCount == null ? 43 : ((Object)$retryCount).hashCode());
        Integer $maxRetry = this.getMaxRetry();
        result = result * 59 + ($maxRetry == null ? 43 : ((Object)$maxRetry).hashCode());
        String $taskType = this.getTaskType();
        result = result * 59 + ($taskType == null ? 43 : $taskType.hashCode());
        String $taskKey = this.getTaskKey();
        result = result * 59 + ($taskKey == null ? 43 : $taskKey.hashCode());
        String $payload = this.getPayload();
        result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
        String $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        OffsetDateTime $executeAt = this.getExecuteAt();
        result = result * 59 + ($executeAt == null ? 43 : ((Object)$executeAt).hashCode());
        OffsetDateTime $timeoutAt = this.getTimeoutAt();
        result = result * 59 + ($timeoutAt == null ? 43 : ((Object)$timeoutAt).hashCode());
        String $resultPayload = this.getResultPayload();
        result = result * 59 + ($resultPayload == null ? 43 : $resultPayload.hashCode());
        String $errorMsg = this.getErrorMsg();
        result = result * 59 + ($errorMsg == null ? 43 : $errorMsg.hashCode());
        String $workerNode = this.getWorkerNode();
        result = result * 59 + ($workerNode == null ? 43 : $workerNode.hashCode());
        String $workerTag = this.getWorkerTag();
        result = result * 59 + ($workerTag == null ? 43 : $workerTag.hashCode());
        OffsetDateTime $createdAt = this.getCreatedAt();
        result = result * 59 + ($createdAt == null ? 43 : ((Object)$createdAt).hashCode());
        OffsetDateTime $updatedAt = this.getUpdatedAt();
        result = result * 59 + ($updatedAt == null ? 43 : ((Object)$updatedAt).hashCode());
        return result;
    }

    public String toString() {
        return "AsyncTaskEntity(id=" + this.getId() + ", taskType=" + this.getTaskType() + ", taskKey=" + this.getTaskKey() + ", payload=" + this.getPayload() + ", priority=" + this.getPriority() + ", status=" + this.getStatus() + ", retryCount=" + this.getRetryCount() + ", maxRetry=" + this.getMaxRetry() + ", executeAt=" + String.valueOf(this.getExecuteAt()) + ", timeoutAt=" + String.valueOf(this.getTimeoutAt()) + ", resultPayload=" + this.getResultPayload() + ", errorMsg=" + this.getErrorMsg() + ", workerNode=" + this.getWorkerNode() + ", workerTag=" + this.getWorkerTag() + ", createdAt=" + String.valueOf(this.getCreatedAt()) + ", updatedAt=" + String.valueOf(this.getUpdatedAt()) + ")";
    }
}

