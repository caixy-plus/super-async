package com.superasync.benchmark.metrics;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能测试指标收集器。
 */
@Data
public class BenchmarkMetrics {

    private final String scenarioName;
    private final AtomicInteger submitted = new AtomicInteger(0);
    private final AtomicInteger completed = new AtomicInteger(0);
    private final AtomicInteger failed = new AtomicInteger(0);
    private final AtomicLong totalSubmitTime = new AtomicLong(0);
    private final List<Long> latencies = new CopyOnWriteArrayList<>();
    private volatile long startTime = 0;
    private volatile long endTime = 0;

    public void recordSubmit() {
        submitted.incrementAndGet();
    }

    public void recordCompletion(long latencyMs) {
        completed.incrementAndGet();
        latencies.add(latencyMs);
    }

    public void recordFailure() {
        failed.incrementAndGet();
    }

    public void markStart() {
        this.startTime = System.currentTimeMillis();
    }

    public void markEnd() {
        this.endTime = System.currentTimeMillis();
    }

    public BenchmarkReport generateReport() {
        BenchmarkReport report = new BenchmarkReport();
        report.setScenarioName(scenarioName);
        report.setTotalSubmitted(submitted.get());
        report.setTotalCompleted(completed.get());
        report.setTotalFailed(failed.get());
        report.setDurationMs(endTime - startTime);

        List<Long> sorted = new ArrayList<>(latencies);
        Collections.sort(sorted);
        report.setTotalLatencies(sorted.size());

        if (!sorted.isEmpty()) {
            DoubleSummaryStatistics stats = sorted.stream().mapToDouble(Long::doubleValue).summaryStatistics();
            report.setAvgLatencyMs(stats.getAverage());
            report.setMinLatencyMs(sorted.get(0));
            report.setMaxLatencyMs(sorted.get(sorted.size() - 1));
            report.setP50LatencyMs(percentile(sorted, 0.50));
            report.setP95LatencyMs(percentile(sorted, 0.95));
            report.setP99LatencyMs(percentile(sorted, 0.99));
            report.setP999LatencyMs(percentile(sorted, 0.999));
        }

        if (report.getDurationMs() > 0) {
            report.setThroughputPerSecond(report.getTotalCompleted() * 1000.0 / report.getDurationMs());
        }
        return report;
    }

    private static long percentile(List<Long> sorted, double p) {
        if (sorted.isEmpty()) return 0;
        int index = (int) Math.ceil(p * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }
}
