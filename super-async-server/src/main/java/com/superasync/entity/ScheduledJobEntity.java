package com.superasync.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "scheduled_jobs")
public class ScheduledJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, unique = true, length = 64)
    private String jobName;

    @Column(name = "task_type", nullable = false, length = 64)
    private String taskType;

    @Column(name = "task_key", nullable = false, length = 255)
    private String taskKey;

    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    @Column(name = "cron_expression", nullable = false, length = 64)
    private String cronExpression;

    @Column(name = "worker_tag", length = 64)
    private String workerTag;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "last_trigger_at")
    private OffsetDateTime lastTriggerAt;

    @Column(name = "next_trigger_at")
    private OffsetDateTime nextTriggerAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getWorkerTag() {
        return workerTag;
    }

    public void setWorkerTag(String workerTag) {
        this.workerTag = workerTag;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getLastTriggerAt() {
        return lastTriggerAt;
    }

    public void setLastTriggerAt(OffsetDateTime lastTriggerAt) {
        this.lastTriggerAt = lastTriggerAt;
    }

    public OffsetDateTime getNextTriggerAt() {
        return nextTriggerAt;
    }

    public void setNextTriggerAt(OffsetDateTime nextTriggerAt) {
        this.nextTriggerAt = nextTriggerAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
