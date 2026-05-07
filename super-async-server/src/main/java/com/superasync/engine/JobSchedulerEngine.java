package com.superasync.engine;

import com.superasync.dto.Priority;
import com.superasync.dto.TaskRequest;
import com.superasync.entity.ScheduledJobEntity;
import com.superasync.repository.ScheduledJobRepository;
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
            }
        }
    }

    private void triggerJob(ScheduledJobEntity job, OffsetDateTime triggerAt) {
        // Submit async task
        TaskRequest.TaskRequestBuilder builder = TaskRequest.builder()
                .taskType(job.getTaskType())
                .taskKey(job.getTaskKey())
                .payload(job.getPayload() != null ? job.getPayload() : "{}")
                .priority(Priority.NORMAL)
                .workerTag(job.getWorkerTag());

        Long taskId = taskDispatcher.submit(builder.build());
        log.info("[JobScheduler] Triggered job {} -> async_task id={}, key={}",
                job.getJobName(), taskId, job.getTaskKey());

        // Calculate next trigger time
        CronExpression cron = CronExpression.parse(job.getCronExpression());
        ZonedDateTime next = cron.next(triggerAt.toZonedDateTime());
        OffsetDateTime nextTriggerAt = next != null ? next.toOffsetDateTime() : null;

        jobRepository.updateTriggerTimes(job.getId(), triggerAt, nextTriggerAt);
        log.info("[JobScheduler] Job {} next trigger at {}", job.getJobName(), nextTriggerAt);
    }
}
