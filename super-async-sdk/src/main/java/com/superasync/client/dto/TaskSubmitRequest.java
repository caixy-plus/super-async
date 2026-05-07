package com.superasync.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 任务提交请求（SDK 侧使用）
 */
@Data
@Builder
public class TaskSubmitRequest {

    /** 任务类型 */
    private String taskType;

    /** 业务幂等键（全局唯一） */
    private String taskKey;

    /** 任务参数 JSON */
    private String payload;

    /** 优先级（默认 5） */
    @Builder.Default
    private int priority = 5;

    /** 延迟执行时间（默认立即执行） */
    @Builder.Default
    private Duration delay = Duration.ZERO;

    /** 超时时间（默认 10 分钟） */
    @Builder.Default
    private Duration timeout = Duration.ofMinutes(10);

    /** 最大重试次数（默认 3） */
    @Builder.Default
    private int maxRetry = 3;

    /** Worker 标签（null 表示本地执行） */
    private String workerTag;
}
