package com.superasync.benchmark.metrics;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 全局指标收集器。
 */
@Component
public class BenchmarkCollector {

    private final ConcurrentHashMap<String, BenchmarkMetrics> metricsMap = new ConcurrentHashMap<>();

    public BenchmarkMetrics getMetrics(String scenario) {
        return metricsMap.computeIfAbsent(scenario, BenchmarkMetrics::new);
    }

    public void recordSubmit(String scenario) {
        getMetrics(scenario).recordSubmit();
    }

    public void recordCompletion(String scenario, long latencyMs) {
        getMetrics(scenario).recordCompletion(latencyMs);
    }

    public void recordFailure(String scenario) {
        getMetrics(scenario).recordFailure();
    }

    public List<BenchmarkReport> generateAllReports() {
        return metricsMap.values().stream()
                .map(BenchmarkMetrics::generateReport)
                .collect(Collectors.toList());
    }

    public BenchmarkReport getReport(String scenario) {
        BenchmarkMetrics metrics = metricsMap.get(scenario);
        return metrics != null ? metrics.generateReport() : null;
    }
}
