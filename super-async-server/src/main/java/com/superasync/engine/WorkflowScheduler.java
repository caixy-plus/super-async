/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.type.TypeReference
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Component
 *  org.springframework.transaction.annotation.Transactional
 */
package com.superasync.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superasync.dto.Priority;
import com.superasync.dto.TaskRequest;
import com.superasync.entity.WorkflowInstanceEntity;
import com.superasync.entity.WorkflowNodeInstanceEntity;
import com.superasync.repository.WorkflowInstanceRepository;
import com.superasync.repository.WorkflowNodeInstanceRepository;
import com.superasync.service.TaskDispatcher;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WorkflowScheduler {
    private static final Logger log = LoggerFactory.getLogger(WorkflowScheduler.class);
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowNodeInstanceRepository nodeInstanceRepository;
    private final TaskDispatcher taskDispatcher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay=5000L)
    @Transactional
    public void schedule() {
        List<WorkflowInstanceEntity> instances = this.instanceRepository.findByStatusIn(List.of("PENDING", "RUNNING"));
        if (instances.isEmpty()) {
            return;
        }
        for (WorkflowInstanceEntity instance : instances) {
            try {
                if ("PENDING".equals(instance.getStatus())) {
                    this.instanceRepository.updateStatus(instance.getId(), "RUNNING");
                }
                this.scheduleInstance(instance);
            }
            catch (Exception e) {
                log.error("[WorkflowScheduler] Failed to schedule instance id={}", (Object)instance.getId(), (Object)e);
            }
        }
    }

    private void scheduleInstance(WorkflowInstanceEntity instance) {
        List<WorkflowNodeInstanceEntity> nodes = this.nodeInstanceRepository.findByWorkflowInstanceId(instance.getId());
        Map<String, WorkflowNodeInstanceEntity> nodeMap = nodes.stream().collect(Collectors.toMap(WorkflowNodeInstanceEntity::getNodeId, n -> n));
        for (WorkflowNodeInstanceEntity node : nodes) {
            boolean ready;
            if (!"PENDING".equals(node.getStatus()) || !(ready = this.checkUpstreamReady(node, nodeMap))) continue;
            TaskRequest request = TaskRequest.builder().taskType(node.getTaskType()).taskKey(node.getTaskKey()).payload(node.getPayload()).priority(Priority.NORMAL).maxRetry(node.getMaxRetry()).build();
            Long taskId = this.taskDispatcher.submit(request);
            this.nodeInstanceRepository.markRunning(node.getId(), "RUNNING", taskId);
            log.info("[WorkflowScheduler] Submitted node instanceId={}, nodeId={}, taskId={}", new Object[]{instance.getId(), node.getNodeId(), taskId});
        }
    }

    private boolean checkUpstreamReady(WorkflowNodeInstanceEntity node, Map<String, WorkflowNodeInstanceEntity> nodeMap) {
        String upstreamJson = node.getUpstreamNodes();
        if (upstreamJson == null || upstreamJson.isBlank() || "[]".equals(upstreamJson)) {
            return true;
        }
        try {
            List<String> upstreamIds = this.objectMapper.readValue(upstreamJson, new TypeReference<List<String>>(){});
            for (String upstreamId : upstreamIds) {
                WorkflowNodeInstanceEntity upstream = nodeMap.get(upstreamId);
                if (upstream != null && "SUCCESS".equals(upstream.getStatus())) continue;
                return false;
            }
            return true;
        }
        catch (Exception e) {
            log.error("[WorkflowScheduler] Failed to parse upstream nodes for nodeId={}", (Object)node.getNodeId(), (Object)e);
            return false;
        }
    }

    public WorkflowScheduler(WorkflowInstanceRepository instanceRepository, WorkflowNodeInstanceRepository nodeInstanceRepository, TaskDispatcher taskDispatcher, ObjectMapper objectMapper) {
        this.instanceRepository = instanceRepository;
        this.nodeInstanceRepository = nodeInstanceRepository;
        this.taskDispatcher = taskDispatcher;
        this.objectMapper = objectMapper;
    }
}

