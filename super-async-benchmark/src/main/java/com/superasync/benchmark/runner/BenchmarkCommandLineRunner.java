package com.superasync.benchmark.runner;

import com.superasync.benchmark.metrics.BenchmarkCollector;
import com.superasync.benchmark.metrics.BenchmarkReport;
import com.superasync.benchmark.service.BenchmarkOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Benchmark 启动器 —— 应用启动后自动执行所有测试场景并输出报告。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BenchmarkCommandLineRunner implements CommandLineRunner {

    private final BenchmarkOrchestrator orchestrator;
    private final BenchmarkCollector collector;

    @Override
    public void run(String... args) throws Exception {
        log.info("============================================================");
        log.info("  SuperAsync 性能测试开始");
        log.info("============================================================");

        List<BenchmarkReport> reports = orchestrator.runAll();

        log.info("============================================================");
        log.info("  SuperAsync 性能测试报告");
        log.info("============================================================");

        StringBuilder markdown = new StringBuilder();
        for (BenchmarkReport report : reports) {
            String console = report.toConsole();
            log.info("\n{}", console);
            markdown.append(report.toMarkdown());
        }

        log.info("============================================================");
        log.info("  Markdown 格式报告（可复制到 README）:");
        log.info("============================================================");
        System.out.println(markdown);

        //  graceful shutdown
        Thread.sleep(2000);
        System.exit(0);
    }
}
