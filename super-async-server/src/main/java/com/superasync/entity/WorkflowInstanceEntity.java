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
@Table(name="workflow_instances")
public class WorkflowInstanceEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="definition_id", nullable=false)
    private Long definitionId;
    @Column(name="status", nullable=false, length=20)
    private String status;
    @Column(name="context_payload", columnDefinition="text")
    private String contextPayload;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    public Long getId() {
        return this.id;
    }

    public Long getDefinitionId() {
        return this.definitionId;
    }

    public String getStatus() {
        return this.status;
    }

    public String getContextPayload() {
        return this.contextPayload;
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

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setContextPayload(String contextPayload) {
        this.contextPayload = contextPayload;
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
        if (!(o instanceof WorkflowInstanceEntity)) {
            return false;
        }
        WorkflowInstanceEntity other = (WorkflowInstanceEntity)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$definitionId = this.getDefinitionId();
        Long other$definitionId = other.getDefinitionId();
        if (this$definitionId == null ? other$definitionId != null : !((Object)this$definitionId).equals(other$definitionId)) {
            return false;
        }
        String this$status = this.getStatus();
        String other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) {
            return false;
        }
        String this$contextPayload = this.getContextPayload();
        String other$contextPayload = other.getContextPayload();
        if (this$contextPayload == null ? other$contextPayload != null : !this$contextPayload.equals(other$contextPayload)) {
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
        return other instanceof WorkflowInstanceEntity;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $definitionId = this.getDefinitionId();
        result = result * 59 + ($definitionId == null ? 43 : ((Object)$definitionId).hashCode());
        String $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        String $contextPayload = this.getContextPayload();
        result = result * 59 + ($contextPayload == null ? 43 : $contextPayload.hashCode());
        OffsetDateTime $createdAt = this.getCreatedAt();
        result = result * 59 + ($createdAt == null ? 43 : ((Object)$createdAt).hashCode());
        OffsetDateTime $updatedAt = this.getUpdatedAt();
        result = result * 59 + ($updatedAt == null ? 43 : ((Object)$updatedAt).hashCode());
        return result;
    }

    public String toString() {
        return "WorkflowInstanceEntity(id=" + this.getId() + ", definitionId=" + this.getDefinitionId() + ", status=" + this.getStatus() + ", contextPayload=" + this.getContextPayload() + ", createdAt=" + String.valueOf(this.getCreatedAt()) + ", updatedAt=" + String.valueOf(this.getUpdatedAt()) + ")";
    }
}

