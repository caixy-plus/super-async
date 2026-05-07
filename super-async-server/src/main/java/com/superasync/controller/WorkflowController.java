/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.superasync.controller;

import com.superasync.dto.Result;
import com.superasync.dto.WorkflowDefinitionRequest;
import com.superasync.dto.WorkflowSubmitRequest;
import com.superasync.engine.WorkflowEngine;
import com.superasync.entity.WorkflowDefinitionEntity;
import com.superasync.entity.WorkflowInstanceEntity;
import com.superasync.entity.WorkflowNodeInstanceEntity;
import com.superasync.repository.WorkflowDefinitionRepository;
import com.superasync.repository.WorkflowInstanceRepository;
import com.superasync.repository.WorkflowNodeInstanceRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/v1/workflows"})
public class WorkflowController {
    private final WorkflowEngine workflowEngine;
    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowNodeInstanceRepository nodeInstanceRepository;

    @PostMapping(value={"/definitions"})
    public Result<WorkflowDefinitionEntity> createDefinition(@RequestBody WorkflowDefinitionRequest request) {
        WorkflowDefinitionEntity definition = this.workflowEngine.createDefinition(request.getName(), request.getDescription(), request.getDag());
        return Result.success(definition);
    }

    @GetMapping(value={"/definitions"})
    public Result<List<WorkflowDefinitionEntity>> listDefinitions() {
        return Result.success(this.definitionRepository.findAll());
    }

    @GetMapping(value={"/definitions/{id}"})
    public Result<WorkflowDefinitionEntity> getDefinition(@PathVariable Long id) {
        return this.definitionRepository.findById(id).map(Result::success).orElse(Result.error(404, "\u5b9a\u4e49\u4e0d\u5b58\u5728"));
    }

    @PostMapping(value={"/instances"})
    public Result<WorkflowInstanceEntity> startInstance(@RequestBody WorkflowSubmitRequest request) {
        WorkflowInstanceEntity instance = this.workflowEngine.startInstance(request);
        return Result.success(instance);
    }

    @GetMapping(value={"/instances/{id}"})
    public Result<WorkflowInstanceEntity> getInstance(@PathVariable Long id) {
        return this.instanceRepository.findById(id).map(Result::success).orElse(Result.error(404, "\u5b9e\u4f8b\u4e0d\u5b58\u5728"));
    }

    @GetMapping(value={"/instances/{id}/nodes"})
    public Result<List<WorkflowNodeInstanceEntity>> listNodes(@PathVariable Long id) {
        return Result.success(this.nodeInstanceRepository.findByWorkflowInstanceId(id));
    }

    public WorkflowController(WorkflowEngine workflowEngine, WorkflowDefinitionRepository definitionRepository, WorkflowInstanceRepository instanceRepository, WorkflowNodeInstanceRepository nodeInstanceRepository) {
        this.workflowEngine = workflowEngine;
        this.definitionRepository = definitionRepository;
        this.instanceRepository = instanceRepository;
        this.nodeInstanceRepository = nodeInstanceRepository;
    }
}

