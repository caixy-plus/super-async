/*
 * Decompiled with CFR 0.152.
 */
package com.superasync.dto;

import java.util.List;

public class WorkflowDag {
    private List<Node> nodes;
    private List<Edge> edges;

    public List<Node> getNodes() {
        return this.nodes;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WorkflowDag)) {
            return false;
        }
        WorkflowDag other = (WorkflowDag)o;
        if (!other.canEqual(this)) {
            return false;
        }
        List<Node> this$nodes = this.getNodes();
        List<Node> other$nodes = other.getNodes();
        if (this$nodes == null ? other$nodes != null : !((Object)this$nodes).equals(other$nodes)) {
            return false;
        }
        List<Edge> this$edges = this.getEdges();
        List<Edge> other$edges = other.getEdges();
        return !(this$edges == null ? other$edges != null : !((Object)this$edges).equals(other$edges));
    }

    protected boolean canEqual(Object other) {
        return other instanceof WorkflowDag;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        List<Node> $nodes = this.getNodes();
        result = result * 59 + ($nodes == null ? 43 : ((Object)$nodes).hashCode());
        List<Edge> $edges = this.getEdges();
        result = result * 59 + ($edges == null ? 43 : ((Object)$edges).hashCode());
        return result;
    }

    public String toString() {
        return "WorkflowDag(nodes=" + String.valueOf(this.getNodes()) + ", edges=" + String.valueOf(this.getEdges()) + ")";
    }

    public static class Edge {
        private String from;
        private String to;

        public String getFrom() {
            return this.from;
        }

        public String getTo() {
            return this.to;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Edge)) {
                return false;
            }
            Edge other = (Edge)o;
            if (!other.canEqual(this)) {
                return false;
            }
            String this$from = this.getFrom();
            String other$from = other.getFrom();
            if (this$from == null ? other$from != null : !this$from.equals(other$from)) {
                return false;
            }
            String this$to = this.getTo();
            String other$to = other.getTo();
            return !(this$to == null ? other$to != null : !this$to.equals(other$to));
        }

        protected boolean canEqual(Object other) {
            return other instanceof Edge;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            String $from = this.getFrom();
            result = result * 59 + ($from == null ? 43 : $from.hashCode());
            String $to = this.getTo();
            result = result * 59 + ($to == null ? 43 : $to.hashCode());
            return result;
        }

        public String toString() {
            return "WorkflowDag.Edge(from=" + this.getFrom() + ", to=" + this.getTo() + ")";
        }
    }

    public static class Node {
        private String id;
        private String name;
        private String taskType;
        private String mode;
        private String payload;
        private int maxRetry = 3;

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getTaskType() {
            return this.taskType;
        }

        public String getMode() {
            return this.mode;
        }

        public String getPayload() {
            return this.payload;
        }

        public int getMaxRetry() {
            return this.maxRetry;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setTaskType(String taskType) {
            this.taskType = taskType;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public void setMaxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Node)) {
                return false;
            }
            Node other = (Node)o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (this.getMaxRetry() != other.getMaxRetry()) {
                return false;
            }
            String this$id = this.getId();
            String other$id = other.getId();
            if (this$id == null ? other$id != null : !this$id.equals(other$id)) {
                return false;
            }
            String this$name = this.getName();
            String other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
                return false;
            }
            String this$taskType = this.getTaskType();
            String other$taskType = other.getTaskType();
            if (this$taskType == null ? other$taskType != null : !this$taskType.equals(other$taskType)) {
                return false;
            }
            String this$mode = this.getMode();
            String other$mode = other.getMode();
            if (this$mode == null ? other$mode != null : !this$mode.equals(other$mode)) {
                return false;
            }
            String this$payload = this.getPayload();
            String other$payload = other.getPayload();
            return !(this$payload == null ? other$payload != null : !this$payload.equals(other$payload));
        }

        protected boolean canEqual(Object other) {
            return other instanceof Node;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * 59 + this.getMaxRetry();
            String $id = this.getId();
            result = result * 59 + ($id == null ? 43 : $id.hashCode());
            String $name = this.getName();
            result = result * 59 + ($name == null ? 43 : $name.hashCode());
            String $taskType = this.getTaskType();
            result = result * 59 + ($taskType == null ? 43 : $taskType.hashCode());
            String $mode = this.getMode();
            result = result * 59 + ($mode == null ? 43 : $mode.hashCode());
            String $payload = this.getPayload();
            result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
            return result;
        }

        public String toString() {
            return "WorkflowDag.Node(id=" + this.getId() + ", name=" + this.getName() + ", taskType=" + this.getTaskType() + ", mode=" + this.getMode() + ", payload=" + this.getPayload() + ", maxRetry=" + this.getMaxRetry() + ")";
        }
    }
}

