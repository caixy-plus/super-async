package com.superasync.repository;

import com.superasync.entity.ScheduledJobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJobEntity, Long> {

    Optional<ScheduledJobEntity> findByJobName(String jobName);

    @Query(value = "SELECT * FROM scheduled_jobs WHERE enabled = true AND next_trigger_at <= ?1 ORDER BY next_trigger_at ASC LIMIT ?2 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<ScheduledJobEntity> pollDueJobs(OffsetDateTime now, int limit);

    @Modifying
    @Query("UPDATE ScheduledJobEntity j SET j.lastTriggerAt = ?2, j.nextTriggerAt = ?3 WHERE j.id = ?1")
    int updateTriggerTimes(Long id, OffsetDateTime lastTriggerAt, OffsetDateTime nextTriggerAt);

    @Modifying
    @Query("UPDATE ScheduledJobEntity j SET j.enabled = ?2 WHERE j.id = ?1")
    int updateEnabled(Long id, boolean enabled);

    Page<ScheduledJobEntity> findByEnabled(Boolean enabled, Pageable pageable);
}
