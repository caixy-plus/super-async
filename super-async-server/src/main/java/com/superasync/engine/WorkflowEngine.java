/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonProcessingException
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.event.EventListener
 *  org.springframework.stereotype.Component
 *  org.springframework.transaction.annotation.Transactional
 */
package com.superasync.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superasync.dto.WorkflowDag;
import com.superasync.dto.WorkflowSubmitRequest;
import com.superasync.entity.WorkflowDefinitionEntity;
import com.superasync.entity.WorkflowInstanceEntity;
import com.superasync.entity.WorkflowNodeInstanceEntity;
import com.superasync.event.TaskCompletedEvent;
import com.superasync.repository.WorkflowDefinitionRepository;
import com.superasync.repository.WorkflowInstanceRepository;
import com.superasync.repository.WorkflowNodeInstanceRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WorkflowEngine {
    private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);
    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowNodeInstanceRepository nodeInstanceRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public WorkflowDefinitionEntity createDefinition(String name, String description, WorkflowDag dag) {
        try {
            WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
            entity.setName(name);
            entity.setDescription(description);
            entity.setDagJson(this.objectMapper.writeValueAsString((Object)dag));
            this.definitionRepository.save(entity);
            log.info("[WorkflowEngine] Created definition id={}, name={}", (Object)entity.getId(), (Object)name);
            return entity;
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("DAG \u5e8f\u5217\u5316\u5931\u8d25", e);
        }
    }

    @Transactional
    public WorkflowInstanceEntity startInstance(WorkflowSubmitRequest request) {
        WorkflowDag dag;
        WorkflowDefinitionEntity definition = (WorkflowDefinitionEntity)this.definitionRepository.findById(request.getDefinitionId()).orElseThrow(() -> new RuntimeException("\u5de5\u4f5c\u6d41\u5b9a\u4e49\u4e0d\u5b58\u5728"));
        try {
            dag = (WorkflowDag)this.objectMapper.readValue(definition.getDagJson(), WorkflowDag.class);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("DAG \u89e3\u6790\u5931\u8d25", e);
        }
        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setDefinitionId(definition.getId());
        instance.setStatus("PENDING");
        instance.setContextPayload(request.getContextPayload());
        this.instanceRepository.save(instance);
        Map<String, List<String>> upstreamMap = dag.getEdges().stream().collect(Collectors.groupingBy(WorkflowDag.Edge::getTo, Collectors.mapping(WorkflowDag.Edge::getFrom, Collectors.toList())));
        Map<String, List<String>> downstreamMap = dag.getEdges().stream().collect(Collectors.groupingBy(WorkflowDag.Edge::getFrom, Collectors.mapping(WorkflowDag.Edge::getTo, Collectors.toList())));
        for (WorkflowDag.Node node : dag.getNodes()) {
            WorkflowNodeInstanceEntity nodeInstance = new WorkflowNodeInstanceEntity();
            nodeInstance.setWorkflowInstanceId(instance.getId());
            nodeInstance.setNodeId(node.getId());
            nodeInstance.setNodeName(node.getName());
            nodeInstance.setTaskType(node.getTaskType());
            nodeInstance.setTaskKey("wf:" + instance.getId() + ":" + node.getId());
            nodeInstance.setPayload(node.getPayload());
            nodeInstance.setStatus("PENDING");
            nodeInstance.setMaxRetry(node.getMaxRetry());
            nodeInstance.setExecuteMode(node.getMode() != null ? node.getMode().toUpperCase() : "SERIAL");
            List<String> upstream = upstreamMap.getOrDefault(node.getId(), List.of());
            List<String> downstream = downstreamMap.getOrDefault(node.getId(), List.of());
            try {
                nodeInstance.setUpstreamNodes(this.objectMapper.writeValueAsString(upstream));
                nodeInstance.setDownstreamNodes(this.objectMapper.writeValueAsString(downstream));
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("\u4e0a\u4e0b\u6e38\u5e8f\u5217\u5316\u5931\u8d25", e);
            }
            this.nodeInstanceRepository.save(nodeInstance);
        }
        log.info("[WorkflowEngine] Started instance id={}, definitionId={}", (Object)instance.getId(), (Object)definition.getId());
        return instance;
    }

    @EventListener
    @Transactional
    public void onTaskCompleted(TaskCompletedEvent event) {
        String taskKey = event.getTaskKey();
        if (taskKey == null || !taskKey.startsWith("wf:")) {
            return;
        }
        WorkflowNodeInstanceEntity node = this.nodeInstanceRepository.findByTaskKey(taskKey);
        if (node == null) {
            return;
        }
        String status = event.isSuccess() ? "SUCCESS" : "FAIL";
        this.nodeInstanceRepository.markCompleted(node.getId(), status, event.getResultPayload(), event.getErrorMsg());
        log.info("[WorkflowEngine] Node completed instanceId={}, nodeId={}, status={}", new Object[]{node.getWorkflowInstanceId(), node.getNodeId(), status});
        this.checkWorkflowCompletion(node.getWorkflowInstanceId());
    }

    private void checkWorkflowCompletion(Long instanceId) {
        List<WorkflowNodeInstanceEntity> nodes = this.nodeInstanceRepository.findByWorkflowInstanceId(instanceId);
        boolean allDone = nodes.stream().allMatch(n -> "SUCCESS".equals(n.getStatus()) || "FAIL".equals(n.getStatus()));
        if (allDone) {
            boolean anyFail = nodes.stream().anyMatch(n -> "FAIL".equals(n.getStatus()));
            String finalStatus = anyFail ? "FAIL" : "SUCCESS";
            this.instanceRepository.updateStatus(instanceId, finalStatus);
            log.info("[WorkflowEngine] Instance {} completed with status={}", (Object)instanceId, (Object)finalStatus);
        }
    }

    public WorkflowEngine(WorkflowDefinitionRepository definitionRepository, WorkflowInstanceRepository instanceRepository, WorkflowNodeInstanceRepository nodeInstanceRepository, ObjectMapper objectMapper) {
        this.definitionRepository = definitionRepository;
        this.instanceRepository = instanceRepository;
        this.nodeInstanceRepository = nodeInstanceRepository;
        this.objectMapper = objectMapper;
    }
}

