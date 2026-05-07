package com.superasync.worker.config;

import com.superasync.worker.SuperAsyncWorkerClient;
import com.superasync.worker.SuperAsyncWorkerProperties;
import com.superasync.worker.engine.SuperAsyncWorkerEngine;
import com.superasync.worker.registry.SuperAsyncWorkerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SuperAsync Worker 自动配置
 * <p>业务侧部署 Worker 服务时，配置 superasync.worker.enabled=true 即可自动装配。</p>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(SuperAsyncWorkerProperties.class)
@ConditionalOnProperty(prefix = "superasync.worker", name = "enabled", havingValue = "true")
@ComponentScan(basePackages = "com.superasync.worker")
public class SuperAsyncWorkerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SuperAsyncWorkerClient superAsyncWorkerClient(SuperAsyncWorkerProperties properties) {
        return new SuperAsyncWorkerClient(properties);
    }
}
