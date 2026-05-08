package com.superasync.engine;

import com.superasync.dto.Priority;
import com.superasync.dto.TaskRequest;
import com.superasync.entity.JobExecutionEntity;
import com.superasync.entity.ScheduledJobEntity;
import com.superasync.repository.ScheduledJobRepository;
import com.superasync.service.JobExecutionService;
import com.superasync.service.ScheduledJobLogService;
import com.superasync.service.TaskDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 定时任务调度引擎。
 * <p>由 super-async 单点调度，到时间后创建 async_task 由 worker 执行。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobSchedulerEngine {

    private final ScheduledJobRepository jobRepository;
    private final TaskDispatcher taskDispatcher;
    private final ScheduledJobLogService logService;
    private final JobExecutionService executionService;

    private static final int POLL_BATCH = 50;

    @Scheduled(fixedDelayString = "${superasync.job-scheduler.poll-interval-ms:10000}")
    @Transactional
    public void triggerDueJobs() {
        OffsetDateTime now = OffsetDateTime.now();
        List<ScheduledJobEntity> dueJobs = jobRepository.pollDueJobs(now, POLL_BATCH);
        if (dueJobs.isEmpty()) {
            return;
        }

        log.info("[JobScheduler] Found {} due scheduled jobs", dueJobs.size());

        for (ScheduledJobEntity job : dueJobs) {
            try {
                triggerJob(job, now);
            } catch (Exception e) {
                log.error("[JobScheduler] Failed to trigger job id={}, name={}", job.getId(), job.getJobName(), e);
                logService.log(job.getId(), "ERROR", String.format("Failed to trigger job '%s': %s", job.getJobName(), e.getMessage()));
            }
        }
    }

    private void triggerJob(ScheduledJobEntity job, OffsetDateTime triggerAt) {
        // Create execution record
        JobExecutionEntity execution = executionService.startExecution(job.getId());
        Long executionId = execution.getId();

        // Submit async task
        TaskRequest request = TaskRequest.builder()
                .taskType(job.getTaskType())
                .taskKey(job.getTaskKey())
                .payload(job.getPayload() != null ? job.getPayload() : "{}")
                .priority(Priority.NORMAL)
                .workerTag(job.getWorkerTag())
                .scheduledJobId(job.getId())
                .executionId(executionId)
                .build();

        Long taskId = taskDispatcher.submit(request);
        log.info("[JobScheduler] Triggered job {} -> async_task id={}, key={}",
                job.getJobName(), taskId, job.getTaskKey());
        logService.log(job.getId(), "INFO", String.format("Triggered job '%s' -> async_task id=%d, key=%s", job.getJobName(), taskId, job.getTaskKey()));

        // Calculate next trigger time
        CronExpression cron = CronExpression.parse(job.getCronExpression());
        ZonedDateTime next = cron.next(triggerAt.toZonedDateTime());
        OffsetDateTime nextTriggerAt = next != null ? next.toOffsetDateTime() : null;

        jobRepository.updateTriggerTimes(job.getId(), triggerAt, nextTriggerAt);
        log.info("[JobScheduler] Job {} next trigger at {}", job.getJobName(), nextTriggerAt);
        logService.log(job.getId(), "INFO", String.format("Next trigger for '%s' at %s", job.getJobName(), nextTriggerAt));
    }
}
