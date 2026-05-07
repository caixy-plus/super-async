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

import com.superasync.entity.WorkflowInstanceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowInstanceRepository
extends JpaRepository<WorkflowInstanceEntity, Long> {
    public List<WorkflowInstanceEntity> findByStatusIn(List<String> var1);

    @Modifying
    @Query(value="UPDATE WorkflowInstanceEntity w SET w.status = ?2 WHERE w.id = ?1")
    public int updateStatus(Long var1, String var2);
}

