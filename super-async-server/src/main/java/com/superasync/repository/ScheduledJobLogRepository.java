package com.superasync.repository;

import com.superasync.entity.ScheduledJobLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledJobLogRepository extends JpaRepository<ScheduledJobLogEntity, Long> {
    Page<ScheduledJobLogEntity> findByScheduledJobId(Long scheduledJobId, Pageable pageable);
}
