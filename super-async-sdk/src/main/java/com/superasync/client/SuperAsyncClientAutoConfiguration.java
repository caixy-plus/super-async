package com.superasync.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SuperAsync 客户端自动配置
 * <p>业务侧引入 super-async 依赖后，配置 superasync.client.server-url 即可自动装配客户端。</p>
 */
@Configuration
@EnableConfigurationProperties(SuperAsyncProperties.class)
@ConditionalOnProperty(prefix = "superasync.client", name = "server-url")
public class SuperAsyncClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SuperAsyncClient superAsyncClient(SuperAsyncProperties properties) {
        return new SuperAsyncClient(properties);
    }
}
