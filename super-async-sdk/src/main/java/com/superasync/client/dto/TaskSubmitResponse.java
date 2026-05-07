package com.superasync.client.dto;

import lombok.Data;

/**
 * 任务提交响应（SDK 侧使用）
 */
@Data
public class TaskSubmitResponse {

    private int code;
    private String message;
    private Long data;
}
