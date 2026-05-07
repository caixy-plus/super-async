/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.data.domain.Sort$Direction
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.superasync.controller;

import com.superasync.dto.Result;
import com.superasync.entity.AsyncTaskEntity;
import com.superasync.repository.AsyncTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/v1/tasks"})
public class TaskQueryController {
    private final AsyncTaskRepository taskRepository;

    @GetMapping(value={"/{taskId}"})
    public Result<AsyncTaskEntity> getTask(@PathVariable Long taskId) {
        return this.taskRepository.findById(taskId).map(Result::success).orElse(Result.error(404, "\u4efb\u52a1\u4e0d\u5b58\u5728"));
    }

    @GetMapping(value={"/key/{taskKey}"})
    public Result<AsyncTaskEntity> getTaskByKey(@PathVariable String taskKey) {
        AsyncTaskEntity task = this.taskRepository.findByTaskKey(taskKey);
        if (task == null) {
            return Result.error(404, "\u4efb\u52a1\u4e0d\u5b58\u5728");
        }
        return Result.success(task);
    }

    @GetMapping
    public Result<Page<AsyncTaskEntity>> listTasks(@RequestParam(required=false) String status, @RequestParam(required=false) String taskType, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        PageRequest pageRequest = PageRequest.of((int)page, (int)size, (Sort)Sort.by((Sort.Direction)Sort.Direction.DESC, (String[])new String[]{"createdAt"}));
        Page<AsyncTaskEntity> result = status != null && taskType != null ? this.taskRepository.findByStatusAndTaskType(status, taskType, (Pageable)pageRequest) : (status != null ? this.taskRepository.findByStatus(status, (Pageable)pageRequest) : (taskType != null ? this.taskRepository.findByTaskType(taskType, (Pageable)pageRequest) : this.taskRepository.findAll((Pageable)pageRequest)));
        return Result.success(result);
    }

    public TaskQueryController(AsyncTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}

