package com.superasync.repository;

import com.superasync.entity.JobExecutionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLogEntity, Long> {

    List<JobExecutionLogEntity> findByExecutionRecordIdOrderByLineNumberAsc(Long executionRecordId);

    long countByExecutionRecordId(Long executionRecordId);

    @Modifying
    @Query(value = "DELETE FROM job_execution_logs WHERE execution_record_id IN (SELECT id FROM job_executions WHERE scheduled_job_id = ?1 AND trigger_time < ?2)", nativeQuery = true)
    int deleteLogsByJobIdAndTriggerTimeBefore(Long scheduledJobId, OffsetDateTime cutoff);

    @Modifying
    @Query(value = "DELETE FROM job_execution_logs WHERE execution_record_id IN (SELECT id FROM job_executions WHERE scheduled_job_id = ?1 AND id NOT IN (SELECT id FROM job_executions WHERE scheduled_job_id = ?1 ORDER BY trigger_time DESC LIMIT 100))", nativeQuery = true)
    int deleteLogsForOldExecutions(Long scheduledJobId);

    @Modifying
    @Query(value = "DELETE FROM job_execution_logs WHERE created_at < ?1", nativeQuery = true)
    int deleteLogsBefore(OffsetDateTime cutoff);
}
