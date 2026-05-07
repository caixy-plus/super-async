/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.superasync.controller;

import com.superasync.dto.Priority;
import com.superasync.dto.Result;
import com.superasync.dto.TaskRequest;
import com.superasync.service.TaskDispatcher;
import java.time.Duration;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/v1/tasks"})
public class TaskSubmitController {
    private final TaskDispatcher taskDispatcher;

    @PostMapping
    public Result<Long> submit(@RequestBody Map<String, Object> body) {
        String taskType = (String)body.get("taskType");
        String taskKey = (String)body.get("taskKey");
        String payload = (String)body.get("payload");
        if (taskType == null || taskKey == null) {
            return Result.error(400, "taskType \u548c taskKey \u4e0d\u80fd\u4e3a\u7a7a");
        }
        Object delaySeconds = body.get("delaySeconds");
        Object timeoutSeconds = body.get("timeoutSeconds");
        Object priorityObj = body.get("priority");
        Object maxRetryObj = body.get("maxRetry");
        Object workerTag = body.get("workerTag");
        TaskRequest.TaskRequestBuilder builder = TaskRequest.builder().taskType(taskType).taskKey(taskKey).payload(payload);
        if (delaySeconds instanceof Number) {
            builder.delay(Duration.ofSeconds(((Number)delaySeconds).longValue()));
        }
        if (timeoutSeconds instanceof Number) {
            builder.timeout(Duration.ofSeconds(((Number)timeoutSeconds).longValue()));
        }
        if (priorityObj instanceof Number) {
            int p = ((Number)priorityObj).intValue();
            Priority priority = switch (p) {
                case 1 -> Priority.CRITICAL;
                case 3 -> Priority.HIGH;
                case 7 -> Priority.LOW;
                case 10 -> Priority.BACKGROUND;
                default -> Priority.NORMAL;
            };
            builder.priority(priority);
        }
        if (maxRetryObj instanceof Number) {
            builder.maxRetry(((Number)maxRetryObj).intValue());
        }
        if (workerTag instanceof String) {
            String wt = (String)workerTag;
            builder.workerTag(wt);
        }
        Long taskId = this.taskDispatcher.submit(builder.build());
        return Result.success(taskId);
    }

    public TaskSubmitController(TaskDispatcher taskDispatcher) {
        this.taskDispatcher = taskDispatcher;
    }
}

