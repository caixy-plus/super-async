package com.superasync.worker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * SuperAsync Worker 配置属性
 */
@Data
@ConfigurationProperties(prefix = "superasync.worker")
public class SuperAsyncWorkerProperties {

    /** 是否启用 Worker 模式 */
    private boolean enabled = false;

    /** 调度器服务端地址 */
    private String serverUrl = "http://localhost:8081";

    /** Worker 唯一标识 */
    private String workerId = "default-worker";

    /** 可处理的任务标签列表 */
    private List<String> tags;

    /** 轮询间隔（毫秒） */
    private long pollIntervalMs = 3000;

    /** 执行线程池核心大小 */
    private int corePoolSize = 4;

    /** 连接超时（毫秒） */
    private int connectTimeout = 5000;

    /** 读取超时（毫秒） */
    private int readTimeout = 30000;
}
