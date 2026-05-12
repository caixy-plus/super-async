package com.superasync.service;

import com.superasync.repository.AsyncTaskRepository;
import com.superasync.repository.JobExecutionLogRepository;
import com.superasync.repository.JobExecutionRepository;
import com.superasync.repository.ScheduledJobLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 数据保留清理服务
 * <p>按配置保留期限清理运行期产生的任务记录和日志，配置表（scheduled_jobs）不受影响。</p>
 * <p>job_execution_logs 跟随 job_executions 统一清理。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataRetentionService {

    private final AsyncTaskRepository asyncTaskRepository;
    private final JobExecutionRepository executionRepository;
    private final JobExecutionLogRepository logRepository;
    private final ScheduledJobLogRepository scheduledJobLogRepository;

    @Value("${superasync.retention.enabled:true}")
    private boolean enabled;

    @Value("${superasync.retention.async-tasks-days:30}")
    private int asyncTasksDays;

    @Value("${superasync.retention.executions-days:30}")
    private int executionsDays;

    @Value("${superasync.retention.scheduler-logs-days:30}")
    private int schedulerLogsDays;

    @Scheduled(cron = "${superasync.retention.cron:0 0 2 * * ?}")
    @Transactional
    public void cleanup() {
        if (!enabled) {
            log.info("[DataRetention] Cleanup is disabled, skipping");
            return;
        }

        log.info("[DataRetention] Starting cleanup: asyncTasksDays={}, executionsDays={}, schedulerLogsDays={}",
                asyncTasksDays, executionsDays, schedulerLogsDays);

        // 1. 清理执行日志（子表跟随主表）
        OffsetDateTime executionsCutoff = OffsetDateTime.now().minusDays(executionsDays);
        int jobLogsDeleted = logRepository.deleteLogsBefore(executionsCutoff);

        // 2. 清理执行记录（主表）
        int executionsDeleted = executionRepository.deleteExecutionsBefore(executionsCutoff);

        // 3. 清理已完成异步任务
        OffsetDateTime tasksCutoff = OffsetDateTime.now().minusDays(asyncTasksDays);
        int asyncTasksDeleted = asyncTaskRepository.deleteCompletedTasksBefore(tasksCutoff);

        // 4. 清理调度器日志
        OffsetDateTime schedulerLogsCutoff = OffsetDateTime.now().minusDays(schedulerLogsDays);
        int schedulerLogsDeleted = scheduledJobLogRepository.deleteLogsBefore(schedulerLogsCutoff);

        log.info("[DataRetention] Cleanup complete: job_execution_logs={}, job_executions={}, async_tasks={}, scheduled_job_logs={}",
                jobLogsDeleted, executionsDeleted, asyncTasksDeleted, schedulerLogsDeleted);
    }
}
