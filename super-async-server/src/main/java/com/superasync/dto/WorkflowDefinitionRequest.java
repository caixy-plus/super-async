/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.dto;

import com.superasync.dto.WorkflowDag;

public class WorkflowDefinitionRequest {
    private String name;
    private String description;
    private WorkflowDag dag;

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public WorkflowDag getDag() {
        return this.dag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDag(WorkflowDag dag) {
        this.dag = dag;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WorkflowDefinitionRequest)) {
            return false;
        }
        WorkflowDefinitionRequest other = (WorkflowDefinitionRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        String this$description = this.getDescription();
        String other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) {
            return false;
        }
        WorkflowDag this$dag = this.getDag();
        WorkflowDag other$dag = other.getDag();
        return !(this$dag == null ? other$dag != null : !((Object)this$dag).equals(other$dag));
    }

    protected boolean canEqual(Object other) {
        return other instanceof WorkflowDefinitionRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        WorkflowDag $dag = this.getDag();
        result = result * 59 + ($dag == null ? 43 : ((Object)$dag).hashCode());
        return result;
    }

    public String toString() {
        return "WorkflowDefinitionRequest(name=" + this.getName() + ", description=" + this.getDescription() + ", dag=" + String.valueOf(this.getDag()) + ")";
    }
}

