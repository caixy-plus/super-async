package com.superasync.benchmark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.superasync")
@EnableScheduling
@EnableAsync
public class BenchmarkApplication {
    public static void main(String[] args) {
        SpringApplication.run(BenchmarkApplication.class, args);
    }
}
