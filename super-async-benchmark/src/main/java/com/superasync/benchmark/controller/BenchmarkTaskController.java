package com.superasync.benchmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superasync.annotation.TaskHandler;
import com.superasync.benchmark.dto.BenchmarkPayload;
import com.superasync.benchmark.metrics.BenchmarkCollector;
import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.worker.annotation.SuperAsyncWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Benchmark 任务处理器 —— 同时支持 Server 本地执行和 Worker 远程执行。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BenchmarkTaskController {

    private final BenchmarkCollector collector;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @TaskHandler("BENCHMARK_SIMPLE")
    @SuperAsyncWorker("BENCHMARK_SIMPLE")
    public TaskResult handleSimple(TaskContext ctx) {
        return process(ctx, false);
    }

    @TaskHandler("BENCHMARK_HEAVY")
    @SuperAsyncWorker("BENCHMARK_HEAVY")
    public TaskResult handleHeavy(TaskContext ctx) {
        return process(ctx, true);
    }

    private TaskResult process(TaskContext ctx, boolean heavy) {
        try {
            String payload = ctx.getPayload();
            if (payload == null) payload = "{}";

            BenchmarkPayload bp = null;
            try {
                bp = objectMapper.readValue(payload, BenchmarkPayload.class);
            } catch (Exception e) {
                // 非 BenchmarkPayload 格式（如定时任务直接提交的 payload）
            }

            if (bp != null && bp.getSubmitTime() > 0) {
                long latency = System.currentTimeMillis() - bp.getSubmitTime();
                collector.recordCompletion(bp.getScenario(), latency);
            }

            String realPayload = (bp != null && bp.getRealPayload() != null) ? bp.getRealPayload() : payload;
            if (heavy) {
                String value = realPayload.replaceAll(".*\"durationMs\":\\s*(\\d+).*", "$1");
                if (!value.isEmpty() && value.matches("\\d+")) {
                    Thread.sleep(Long.parseLong(value));
                }
            }
            return TaskResult.ok("ok");
        } catch (Exception e) {
            collector.recordFailure(ctx.getTaskType());
            return TaskResult.fail(e.getMessage());
        }
    }
}
