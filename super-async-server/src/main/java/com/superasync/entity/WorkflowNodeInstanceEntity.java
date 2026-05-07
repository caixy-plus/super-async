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

@Entity
@Table(name="workflow_node_instances")
public class WorkflowNodeInstanceEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="workflow_instance_id", nullable=false)
    private Long workflowInstanceId;
    @Column(name="node_id", nullable=false, length=64)
    private String nodeId;
    @Column(name="node_name", length=128)
    private String nodeName;
    @Column(name="task_type", nullable=false, length=64)
    private String taskType;
    @Column(name="task_key", unique=true, length=255)
    private String taskKey;
    @Column(name="payload", columnDefinition="text")
    private String payload;
    @Column(name="status", nullable=false, length=20)
    private String status;
    @Column(name="retry_count", nullable=false)
    private Integer retryCount = 0;
    @Column(name="max_retry", nullable=false)
    private Integer maxRetry = 3;
    @Column(name="upstream_nodes", columnDefinition="text")
    private String upstreamNodes;
    @Column(name="downstream_nodes", columnDefinition="text")
    private String downstreamNodes;
    @Column(name="execute_mode", nullable=false, length=10)
    private String executeMode = "SERIAL";
    @Column(name="task_id")
    private Long taskId;
    @Column(name="result_payload", columnDefinition="text")
    private String resultPayload;
    @Column(name="error_msg", columnDefinition="text")
    private String errorMsg;
    @Column(name="started_at")
    private OffsetDateTime startedAt;
    @Column(name="completed_at")
    private OffsetDateTime completedAt;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return this.id;
    }

    public Long getWorkflowInstanceId() {
        return this.workflowInstanceId;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public String getNodeName() {
        return this.nodeName;
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

    public String getStatus() {
        return this.status;
    }

    public Integer getRetryCount() {
        return this.retryCount;
    }

    public Integer getMaxRetry() {
        return this.maxRetry;
    }

    public String getUpstreamNodes() {
        return this.upstreamNodes;
    }

    public String getDownstreamNodes() {
        return this.downstreamNodes;
    }

    public String getExecuteMode() {
        return this.executeMode;
    }

    public Long getTaskId() {
        return this.taskId;
    }

    public String getResultPayload() {
        return this.resultPayload;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public OffsetDateTime getStartedAt() {
        return this.startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return this.completedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setWorkflowInstanceId(Long workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void setMaxRetry(Integer maxRetry) {
        this.maxRetry = maxRetry;
    }

    public void setUpstreamNodes(String upstreamNodes) {
        this.upstreamNodes = upstreamNodes;
    }

    public void setDownstreamNodes(String downstreamNodes) {
        this.downstreamNodes = downstreamNodes;
    }

    public void setExecuteMode(String executeMode) {
        this.executeMode = executeMode;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public void setResultPayload(String resultPayload) {
        this.resultPayload = resultPayload;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WorkflowNodeInstanceEntity)) {
            return false;
        }
        WorkflowNodeInstanceEntity other = (WorkflowNodeInstanceEntity)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$workflowInstanceId = this.getWorkflowInstanceId();
        Long other$workflowInstanceId = other.getWorkflowInstanceId();
        if (this$workflowInstanceId == null ? other$workflowInstanceId != null : !((Object)this$workflowInstanceId).equals(other$workflowInstanceId)) {
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
        Long this$taskId = this.getTaskId();
        Long other$taskId = other.getTaskId();
        if (this$taskId == null ? other$taskId != null : !((Object)this$taskId).equals(other$taskId)) {
            return false;
        }
        String this$nodeId = this.getNodeId();
        String other$nodeId = other.getNodeId();
        if (this$nodeId == null ? other$nodeId != null : !this$nodeId.equals(other$nodeId)) {
            return false;
        }
        String this$nodeName = this.getNodeName();
        String other$nodeName = other.getNodeName();
        if (this$nodeName == null ? other$nodeName != null : !this$nodeName.equals(other$nodeName)) {
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
        String this$upstreamNodes = this.getUpstreamNodes();
        String other$upstreamNodes = other.getUpstreamNodes();
        if (this$upstreamNodes == null ? other$upstreamNodes != null : !this$upstreamNodes.equals(other$upstreamNodes)) {
            return false;
        }
        String this$downstreamNodes = this.getDownstreamNodes();
        String other$downstreamNodes = other.getDownstreamNodes();
        if (this$downstreamNodes == null ? other$downstreamNodes != null : !this$downstreamNodes.equals(other$downstreamNodes)) {
            return false;
        }
        String this$executeMode = this.getExecuteMode();
        String other$executeMode = other.getExecuteMode();
        if (this$executeMode == null ? other$executeMode != null : !this$executeMode.equals(other$executeMode)) {
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
        OffsetDateTime this$startedAt = this.getStartedAt();
        OffsetDateTime other$startedAt = other.getStartedAt();
        if (this$startedAt == null ? other$startedAt != null : !((Object)this$startedAt).equals(other$startedAt)) {
            return false;
        }
        OffsetDateTime this$completedAt = this.getCompletedAt();
        OffsetDateTime other$completedAt = other.getCompletedAt();
        if (this$completedAt == null ? other$completedAt != null : !((Object)this$completedAt).equals(other$completedAt)) {
            return false;
        }
        OffsetDateTime this$createdAt = this.getCreatedAt();
        OffsetDateTime other$createdAt = other.getCreatedAt();
        return !(this$createdAt == null ? other$createdAt != null : !((Object)this$createdAt).equals(other$createdAt));
    }

    protected boolean canEqual(Object other) {
        return other instanceof WorkflowNodeInstanceEntity;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $workflowInstanceId = this.getWorkflowInstanceId();
        result = result * 59 + ($workflowInstanceId == null ? 43 : ((Object)$workflowInstanceId).hashCode());
        Integer $retryCount = this.getRetryCount();
        result = result * 59 + ($retryCount == null ? 43 : ((Object)$retryCount).hashCode());
        Integer $maxRetry = this.getMaxRetry();
        result = result * 59 + ($maxRetry == null ? 43 : ((Object)$maxRetry).hashCode());
        Long $taskId = this.getTaskId();
        result = result * 59 + ($taskId == null ? 43 : ((Object)$taskId).hashCode());
        String $nodeId = this.getNodeId();
        result = result * 59 + ($nodeId == null ? 43 : $nodeId.hashCode());
        String $nodeName = this.getNodeName();
        result = result * 59 + ($nodeName == null ? 43 : $nodeName.hashCode());
        String $taskType = this.getTaskType();
        result = result * 59 + ($taskType == null ? 43 : $taskType.hashCode());
        String $taskKey = this.getTaskKey();
        result = result * 59 + ($taskKey == null ? 43 : $taskKey.hashCode());
        String $payload = this.getPayload();
        result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
        String $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        String $upstreamNodes = this.getUpstreamNodes();
        result = result * 59 + ($upstreamNodes == null ? 43 : $upstreamNodes.hashCode());
        String $downstreamNodes = this.getDownstreamNodes();
        result = result * 59 + ($downstreamNodes == null ? 43 : $downstreamNodes.hashCode());
        String $executeMode = this.getExecuteMode();
        result = result * 59 + ($executeMode == null ? 43 : $executeMode.hashCode());
        String $resultPayload = this.getResultPayload();
        result = result * 59 + ($resultPayload == null ? 43 : $resultPayload.hashCode());
        String $errorMsg = this.getErrorMsg();
        result = result * 59 + ($errorMsg == null ? 43 : $errorMsg.hashCode());
        OffsetDateTime $startedAt = this.getStartedAt();
        result = result * 59 + ($startedAt == null ? 43 : ((Object)$startedAt).hashCode());
        OffsetDateTime $completedAt = this.getCompletedAt();
        result = result * 59 + ($completedAt == null ? 43 : ((Object)$completedAt).hashCode());
        OffsetDateTime $createdAt = this.getCreatedAt();
        result = result * 59 + ($createdAt == null ? 43 : ((Object)$createdAt).hashCode());
        return result;
    }

    public String toString() {
        return "WorkflowNodeInstanceEntity(id=" + this.getId() + ", workflowInstanceId=" + this.getWorkflowInstanceId() + ", nodeId=" + this.getNodeId() + ", nodeName=" + this.getNodeName() + ", taskType=" + this.getTaskType() + ", taskKey=" + this.getTaskKey() + ", payload=" + this.getPayload() + ", status=" + this.getStatus() + ", retryCount=" + this.getRetryCount() + ", maxRetry=" + this.getMaxRetry() + ", upstreamNodes=" + this.getUpstreamNodes() + ", downstreamNodes=" + this.getDownstreamNodes() + ", executeMode=" + this.getExecuteMode() + ", taskId=" + this.getTaskId() + ", resultPayload=" + this.getResultPayload() + ", errorMsg=" + this.getErrorMsg() + ", startedAt=" + String.valueOf(this.getStartedAt()) + ", completedAt=" + String.valueOf(this.getCompletedAt()) + ", createdAt=" + String.valueOf(this.getCreatedAt()) + ")";
    }
}

