package com.superasync.worker.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.superasync.worker.SuperAsyncWorkerClient;
import com.superasync.worker.SuperAsyncWorkerProperties;
import com.superasync.worker.logging.SuperAsyncLogbackAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

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

    @PostConstruct
    public void registerLogbackAppender() {
        try {
            if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext lc)) {
                return;
            }
            Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
            if (root.getAppender("SUPER_ASYNC") != null) {
                return;
            }
            SuperAsyncLogbackAppender appender = new SuperAsyncLogbackAppender();
            appender.setContext(lc);
            appender.setName("SUPER_ASYNC");
            appender.start();
            root.addAppender(appender);
        } catch (Exception e) {
            // Logback 不存在时静默忽略
        }
    }
}
