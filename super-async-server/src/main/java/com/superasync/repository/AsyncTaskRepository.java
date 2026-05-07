/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.stereotype.Repository
 */
package com.superasync.repository;

import com.superasync.entity.AsyncTaskEntity;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AsyncTaskRepository
extends JpaRepository<AsyncTaskEntity, Long> {
    public AsyncTaskEntity findByTaskKey(String var1);

    @Query("SELECT t FROM AsyncTaskEntity t WHERE t.id = ?1")
    public AsyncTaskEntity findByTaskId(Long var1);

    @Query(value="SELECT * FROM async_tasks\nWHERE status IN ('PENDING', 'FAIL')\n  AND execute_at <= ?1\n  AND retry_count < max_retry\n  AND worker_tag IS NULL\nORDER BY priority ASC, execute_at ASC\nLIMIT ?2 FOR UPDATE SKIP LOCKED\n", nativeQuery=true)
    public List<AsyncTaskEntity> pollLocalTasks(OffsetDateTime var1, int var2);

    @Query(value="SELECT * FROM async_tasks\nWHERE status IN ('PENDING', 'FAIL')\n  AND execute_at <= ?1\n  AND retry_count < max_retry\n  AND worker_tag = ?2\nORDER BY priority ASC, execute_at ASC\nLIMIT 1 FOR UPDATE SKIP LOCKED\n", nativeQuery=true)
    public AsyncTaskEntity pollWorkerTask(OffsetDateTime var1, String var2);

    @Query(value="SELECT * FROM async_tasks\nWHERE status IN ('PENDING', 'FAIL')\n  AND execute_at <= ?1\n  AND retry_count < max_retry\n  AND worker_tag = ?2\nORDER BY priority ASC, execute_at ASC\nLIMIT ?3 FOR UPDATE SKIP LOCKED\n", nativeQuery=true)
    public List<AsyncTaskEntity> pollWorkerTasks(OffsetDateTime var1, String var2, int var3);

    @Query(value="SELECT * FROM async_tasks\nWHERE status = 'PROCESSING'\n  AND timeout_at <= ?1\n  AND worker_tag IS NULL\nLIMIT ?2 FOR UPDATE SKIP LOCKED\n", nativeQuery=true)
    public List<AsyncTaskEntity> pollTimeoutTasks(OffsetDateTime var1, int var2);

    @Modifying
    @Query(value="UPDATE AsyncTaskEntity t SET t.status = 'PROCESSING', t.workerNode = ?2 WHERE t.id = ?1")
    public int lockTask(Long var1, String var2);

    @Modifying
    @Query(value="UPDATE AsyncTaskEntity t SET t.status = ?2, t.resultPayload = ?3, t.errorMsg = ?4, t.workerNode = null WHERE t.id = ?1")
    public int completeTask(Long var1, String var2, String var3, String var4);

    @Modifying
    @Query(value="UPDATE AsyncTaskEntity t SET t.status = 'PENDING', t.retryCount = t.retryCount + 1, t.executeAt = ?2, t.workerNode = null WHERE t.id = ?1")
    public int markForRetry(Long var1, OffsetDateTime var2);

    public Page<AsyncTaskEntity> findByStatus(String var1, Pageable var2);

    public Page<AsyncTaskEntity> findByTaskType(String var1, Pageable var2);

    public Page<AsyncTaskEntity> findByStatusAndTaskType(String var1, String var2, Pageable var3);
}

