package com.superasync.service;

import com.superasync.entity.JobExecutionEntity;
import com.superasync.entity.JobExecutionLogEntity;
import com.superasync.repository.JobExecutionLogRepository;
import com.superasync.repository.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutionService {

    private final JobExecutionRepository executionRepository;
    private final JobExecutionLogRepository logRepository;
    private final SseEmitterManager sseEmitterManager;

    @org.springframework.beans.factory.annotation.Value("${superasync.retention.executions-days:30}")
    private int retentionDays;

    @Transactional
    public JobExecutionEntity startExecution(Long scheduledJobId) {
        cleanupOldExecutions(scheduledJobId);

        JobExecutionEntity record = new JobExecutionEntity();
        record.setScheduledJobId(scheduledJobId);
        record.setTriggerTime(OffsetDateTime.now());
        record.setStatus("PENDING");
        return executionRepository.save(record);
    }

    @Transactional
    public void markProcessing(Long executionId) {
        executionRepository.findById(executionId).ifPresent(e -> {
            e.setStatus("PROCESSING");
            e.setStartTime(OffsetDateTime.now());
            executionRepository.save(e);
        });
    }

    @Transactional
    public void markCompleted(Long executionId, boolean success, String errorMsg) {
        executionRepository.findById(executionId).ifPresent(e -> {
            e.setStatus(success ? "SUCCESS" : "FAIL");
            e.setEndTime(OffsetDateTime.now());
            e.setErrorMsg(errorMsg);
            executionRepository.save(e);
        });
    }

    @Transactional
    public void appendLog(Long executionId, String level, String message) {
        long count = logRepository.countByExecutionRecordId(executionId);
        JobExecutionLogEntity log = new JobExecutionLogEntity();
        log.setExecutionRecordId(executionId);
        log.setLineNumber((int) count + 1);
        log.setLevel(level);
        log.setMessage(message);
        logRepository.save(log);

        sseEmitterManager.send(executionId, level, message);
    }

    public Page<JobExecutionEntity> listByJobId(Long scheduledJobId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "triggerTime"));
        return executionRepository.findByScheduledJobId(scheduledJobId, pr);
    }

    public List<JobExecutionLogEntity> listLogs(Long executionId) {
        return logRepository.findByExecutionRecordIdOrderByLineNumberAsc(executionId);
    }

    @Transactional
    public void cleanupOldExecutions(Long scheduledJobId) {
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
        logRepository.deleteLogsByJobIdAndTriggerTimeBefore(scheduledJobId, sevenDaysAgo);
        executionRepository.deleteByJobIdAndTriggerTimeBefore(scheduledJobId, sevenDaysAgo);

        // 超时超过 1 小时仍未执行的 PENDING 记录标记为 TIMEOUT
        OffsetDateTime oneHourAgo = OffsetDateTime.now().minusHours(1);
        int timedOut = executionRepository.timeoutStalePendingExecutions(scheduledJobId, oneHourAgo);
        if (timedOut > 0) {
            log.warn("[JobExecution] Marked {} stale PENDING execution(s) as TIMEOUT for jobId={}", timedOut, scheduledJobId);
        }

        long count = executionRepository.countByScheduledJobId(scheduledJobId);
        if (count >= 100) {
            logRepository.deleteLogsForOldExecutions(scheduledJobId);
            executionRepository.keepOnlyLatestExecutions(scheduledJobId);
        }
    }
}
