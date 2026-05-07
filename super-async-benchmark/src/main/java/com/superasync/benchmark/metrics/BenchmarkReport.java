package com.superasync.benchmark.metrics;

import lombok.Data;

/**
 * 性能测试报告单场景结果。
 */
@Data
public class BenchmarkReport {

    private String scenarioName;
    private int totalSubmitted;
    private int totalCompleted;
    private int totalFailed;
    private int totalLatencies;
    private long durationMs;
    private double throughputPerSecond;
    private double avgLatencyMs;
    private long minLatencyMs;
    private long maxLatencyMs;
    private long p50LatencyMs;
    private long p95LatencyMs;
    private long p99LatencyMs;
    private long p999LatencyMs;

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(scenarioName).append("\n\n");
        sb.append("| 指标 | 数值 |\n");
        sb.append("|---|---|\n");
        sb.append("| 提交任务数 | ").append(totalSubmitted).append(" |\n");
        sb.append("| 完成任务数 | ").append(totalCompleted).append(" |\n");
        sb.append("| 失败任务数 | ").append(totalFailed).append(" |\n");
        sb.append("| 总耗时 | ").append(String.format("%.2f s", durationMs / 1000.0)).append(" |\n");
        sb.append("| 吞吐量 | ").append(String.format("%.2f tasks/s", throughputPerSecond)).append(" |\n");
        sb.append("| 平均延迟 | ").append(String.format("%.2f ms", avgLatencyMs)).append(" |\n");
        sb.append("| 最小延迟 | ").append(minLatencyMs).append(" ms |\n");
        sb.append("| P50 延迟 | ").append(p50LatencyMs).append(" ms |\n");
        sb.append("| P95 延迟 | ").append(p95LatencyMs).append(" ms |\n");
        sb.append("| P99 延迟 | ").append(p99LatencyMs).append(" ms |\n");
        sb.append("| P99.9 延迟 | ").append(p999LatencyMs).append(" ms |\n");
        sb.append("| 最大延迟 | ").append(maxLatencyMs).append(" ms |\n");
        sb.append("\n");
        return sb.toString();
    }

    public String toConsole() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n============================================================\n");
        sb.append("  场景: ").append(scenarioName).append("\n");
        sb.append("============================================================\n");
        sb.append(String.format("  提交任务数 : %d%n", totalSubmitted));
        sb.append(String.format("  完成任务数 : %d%n", totalCompleted));
        sb.append(String.format("  失败任务数 : %d%n", totalFailed));
        sb.append(String.format("  总耗时     : %.2f s%n", durationMs / 1000.0));
        sb.append(String.format("  吞吐量     : %.2f tasks/s%n", throughputPerSecond));
        sb.append(String.format("  平均延迟   : %.2f ms%n", avgLatencyMs));
        sb.append(String.format("  最小延迟   : %d ms%n", minLatencyMs));
        sb.append(String.format("  P50 延迟   : %d ms%n", p50LatencyMs));
        sb.append(String.format("  P95 延迟   : %d ms%n", p95LatencyMs));
        sb.append(String.format("  P99 延迟   : %d ms%n", p99LatencyMs));
        sb.append(String.format("  P99.9 延迟 : %d ms%n", p999LatencyMs));
        sb.append(String.format("  最大延迟   : %d ms%n", maxLatencyMs));
        sb.append("============================================================\n");
        return sb.toString();
    }
}
