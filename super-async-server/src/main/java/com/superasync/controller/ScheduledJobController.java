package com.superasync.controller;

import com.superasync.dto.Result;
import com.superasync.entity.ScheduledJobEntity;
import com.superasync.entity.ScheduledJobLogEntity;
import com.superasync.repository.ScheduledJobRepository;
import com.superasync.service.ScheduledJobLogService;
import com.superasync.service.TaskDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/v1/scheduled-jobs")
@RequiredArgsConstructor
public class ScheduledJobController {

    private final ScheduledJobRepository jobRepository;
    private final TaskDispatcher taskDispatcher;
    private final ScheduledJobLogService logService;

    @GetMapping
    public Result<Page<ScheduledJobEntity>> list(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ScheduledJobEntity> result = enabled != null
                ? jobRepository.findByEnabled(enabled, pageable)
                : jobRepository.findAll(pageable);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ScheduledJobEntity> getById(@PathVariable Long id) {
        return jobRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error(404, "任务不存在"));
    }

    @PostMapping
    public Result<Long> create(@RequestBody ScheduledJobEntity job) {
        if (job.getJobName() == null || job.getTaskType() == null || job.getTaskKey() == null || job.getCronExpression() == null) {
            return Result.error(400, "jobName, taskType, taskKey, cronExpression 不能为空");
        }
        if (!isValidCron(job.getCronExpression())) {
            return Result.error(400, "cron 表达式格式错误");
        }
        Optional<ScheduledJobEntity> existing = jobRepository.findByJobName(job.getJobName());
        if (existing.isPresent()) {
            log.info("[ScheduledJob] Job name={} already exists, id={}", job.getJobName(), existing.get().getId());
            return Result.success(existing.get().getId());
        }

        // Compute initial nextTriggerAt
        CronExpression cron = CronExpression.parse(job.getCronExpression());
        ZonedDateTime next = cron.next(ZonedDateTime.now());
        if (next != null) {
            job.setNextTriggerAt(next.toOffsetDateTime());
        }

        ScheduledJobEntity saved = jobRepository.save(job);
        log.info("[ScheduledJob] Created job id={}, name={}", saved.getId(), saved.getJobName());
        return Result.success(saved.getId());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ScheduledJobEntity updates) {
        ScheduledJobEntity job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            return Result.error(404, "任务不存在");
        }

        if (updates.getCronExpression() != null) {
            if (!isValidCron(updates.getCronExpression())) {
                return Result.error(400, "cron 表达式格式错误");
            }
            job.setCronExpression(updates.getCronExpression());
            CronExpression cron = CronExpression.parse(updates.getCronExpression());
            ZonedDateTime next = cron.next(ZonedDateTime.now());
            job.setNextTriggerAt(next != null
                    ? next.toOffsetDateTime()
                    : null);
        }
        if (updates.getTaskType() != null) job.setTaskType(updates.getTaskType());
        if (updates.getTaskKey() != null) job.setTaskKey(updates.getTaskKey());
        if (updates.getPayload() != null) job.setPayload(updates.getPayload());
        if (updates.getWorkerTag() != null) job.setWorkerTag(updates.getWorkerTag());
        if (updates.getDescription() != null) job.setDescription(updates.getDescription());
        if (updates.getEnabled() != null) job.setEnabled(updates.getEnabled());

        jobRepository.save(job);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!jobRepository.existsById(id)) {
            return Result.error(404, "任务不存在");
        }
        jobRepository.deleteById(id);
        return Result.success();
    }

    @PostMapping("/{id}/trigger")
    public Result<Long> triggerNow(@PathVariable Long id) {
        ScheduledJobEntity job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            return Result.error(404, "任务不存在");
        }
        if (!Boolean.TRUE.equals(job.getEnabled())) {
            return Result.error(400, "任务已禁用");
        }

        com.superasync.dto.TaskRequest request = com.superasync.dto.TaskRequest.builder()
                .taskType(job.getTaskType())
                .taskKey(job.getTaskKey() + ":manual:" + System.currentTimeMillis())
                .payload(job.getPayload())
                .priority(com.superasync.dto.Priority.NORMAL)
                .workerTag(job.getWorkerTag())
                .build();
        Long taskId = taskDispatcher.submit(request);
        log.info("[ScheduledJob] Manual trigger job id={}, name={} -> taskId={}", id, job.getJobName(), taskId);
        return Result.success(taskId);
    }

    @PostMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return Result.error(400, "enabled 不能为空");
        }
        int updated = jobRepository.updateEnabled(id, enabled);
        if (updated == 0) {
            return Result.error(404, "任务不存在");
        }
        return Result.success();
    }

    @GetMapping("/{id}/logs")
    public Result<Page<ScheduledJobLogEntity>> getJobLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!jobRepository.existsById(id)) {
            return Result.error(404, "任务不存在");
        }
        return Result.success(logService.listByJobId(id, page, size));
    }

    private boolean isValidCron(String cron) {
        try {
            CronExpression.parse(cron);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
