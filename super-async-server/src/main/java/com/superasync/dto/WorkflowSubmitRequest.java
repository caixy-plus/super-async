/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.dto;

public class WorkflowSubmitRequest {
    private Long definitionId;
    private String contextPayload;

    public Long getDefinitionId() {
        return this.definitionId;
    }

    public String getContextPayload() {
        return this.contextPayload;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    public void setContextPayload(String contextPayload) {
        this.contextPayload = contextPayload;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WorkflowSubmitRequest)) {
            return false;
        }
        WorkflowSubmitRequest other = (WorkflowSubmitRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$definitionId = this.getDefinitionId();
        Long other$definitionId = other.getDefinitionId();
        if (this$definitionId == null ? other$definitionId != null : !((Object)this$definitionId).equals(other$definitionId)) {
            return false;
        }
        String this$contextPayload = this.getContextPayload();
        String other$contextPayload = other.getContextPayload();
        return !(this$contextPayload == null ? other$contextPayload != null : !this$contextPayload.equals(other$contextPayload));
    }

    protected boolean canEqual(Object other) {
        return other instanceof WorkflowSubmitRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $definitionId = this.getDefinitionId();
        result = result * 59 + ($definitionId == null ? 43 : ((Object)$definitionId).hashCode());
        String $contextPayload = this.getContextPayload();
        result = result * 59 + ($contextPayload == null ? 43 : $contextPayload.hashCode());
        return result;
    }

    public String toString() {
        return "WorkflowSubmitRequest(definitionId=" + this.getDefinitionId() + ", contextPayload=" + this.getContextPayload() + ")";
    }
}

