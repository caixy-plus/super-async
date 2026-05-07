/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.stereotype.Repository
 */
package com.superasync.repository;

import com.superasync.entity.WorkflowNodeInstanceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowNodeInstanceRepository
extends JpaRepository<WorkflowNodeInstanceEntity, Long> {
    public List<WorkflowNodeInstanceEntity> findByWorkflowInstanceId(Long var1);

    public WorkflowNodeInstanceEntity findByTaskKey(String var1);

    @Modifying
    @Query(value="UPDATE WorkflowNodeInstanceEntity n SET n.status = ?2, n.taskId = ?3, n.startedAt = CURRENT_TIMESTAMP WHERE n.id = ?1")
    public int markRunning(Long var1, String var2, Long var3);

    @Modifying
    @Query(value="UPDATE WorkflowNodeInstanceEntity n SET n.status = ?2, n.resultPayload = ?3, n.errorMsg = ?4, n.completedAt = CURRENT_TIMESTAMP WHERE n.id = ?1")
    public int markCompleted(Long var1, String var2, String var3, String var4);
}

