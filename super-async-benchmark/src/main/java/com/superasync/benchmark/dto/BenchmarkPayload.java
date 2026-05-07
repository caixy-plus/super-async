package com.superasync.benchmark.dto;

import lombok.Data;

/**
 * Benchmark 任务负载元数据。
 */
@Data
public class BenchmarkPayload {
    private long submitTime;
    private String scenario;
    private int seq;
    private String realPayload;
}
