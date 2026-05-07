package com.superasync.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SuperAsync 客户端配置属性
 */
@Data
@ConfigurationProperties(prefix = "superasync.client")
public class SuperAsyncProperties {

    /** SuperAsync 服务端地址，例如 http://localhost:8081 */
    private String serverUrl = "http://localhost:8081";

    /** 连接超时（毫秒） */
    private int connectTimeout = 5000;

    /** 读取超时（毫秒） */
    private int readTimeout = 10000;
}
