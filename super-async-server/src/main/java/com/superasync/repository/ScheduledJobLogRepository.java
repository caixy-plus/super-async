package com.superasync.repository;

import com.superasync.entity.ScheduledJobLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface ScheduledJobLogRepository extends JpaRepository<ScheduledJobLogEntity, Long> {
    Page<ScheduledJobLogEntity> findByScheduledJobId(Long scheduledJobId, Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM scheduled_job_logs WHERE created_at < ?1", nativeQuery = true)
    int deleteLogsBefore(OffsetDateTime cutoff);
}
