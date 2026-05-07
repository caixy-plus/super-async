package com.superasync.client.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 定时任务注册请求
 */
@Data
@Builder
public class ScheduledJobRequest {

    private String jobName;
    private String taskType;
    private String taskKey;
    private String payload;
    private String cronExpression;
    private String workerTag;
    private String description;
}
