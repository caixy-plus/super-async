package com.superasync.repository;

import com.superasync.entity.JobExecutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecutionEntity, Long> {

    Page<JobExecutionEntity> findByScheduledJobId(Long scheduledJobId, Pageable pageable);

    long countByScheduledJobId(Long scheduledJobId);

    @Modifying
    @Query(value = "DELETE FROM job_executions WHERE scheduled_job_id = ?1 AND trigger_time < ?2", nativeQuery = true)
    int deleteByJobIdAndTriggerTimeBefore(Long scheduledJobId, OffsetDateTime cutoff);

    @Modifying
    @Query(value = "DELETE FROM job_executions WHERE scheduled_job_id = ?1 AND id NOT IN (SELECT id FROM job_executions WHERE scheduled_job_id = ?1 ORDER BY trigger_time DESC LIMIT 100)", nativeQuery = true)
    int keepOnlyLatestExecutions(Long scheduledJobId);

    @Modifying
    @Query(value = "UPDATE job_executions SET status = 'TIMEOUT', end_time = NOW() WHERE scheduled_job_id = ?1 AND status = 'PENDING' AND trigger_time < ?2", nativeQuery = true)
    int timeoutStalePendingExecutions(Long scheduledJobId, OffsetDateTime cutoff);

    @Modifying
    @Query(value = "DELETE FROM job_executions WHERE trigger_time < ?1", nativeQuery = true)
    int deleteExecutionsBefore(OffsetDateTime cutoff);
}
