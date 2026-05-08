package com.superasync.controller;

import com.superasync.dto.Result;
import com.superasync.entity.JobExecutionEntity;
import com.superasync.entity.JobExecutionLogEntity;
import com.superasync.repository.ScheduledJobRepository;
import com.superasync.service.JobExecutionService;
import com.superasync.service.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/executions")
@RequiredArgsConstructor
public class JobExecutionController {

    private final JobExecutionService executionService;
    private final ScheduledJobRepository jobRepository;
    private final SseEmitterManager sseEmitterManager;

    @GetMapping("/scheduled-job/{scheduledJobId}")
    public Result<Page<JobExecutionEntity>> listByJob(
            @PathVariable Long scheduledJobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!jobRepository.existsById(scheduledJobId)) {
            return Result.error(404, "任务不存在");
        }
        return Result.success(executionService.listByJobId(scheduledJobId, page, size));
    }

    @GetMapping("/{executionId}/logs")
    public Result<List<JobExecutionLogEntity>> listLogs(@PathVariable Long executionId) {
        return Result.success(executionService.listLogs(executionId));
    }

    @GetMapping(value = "/{executionId}/logs/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs(@PathVariable Long executionId) {
        SseEmitter emitter = new SseEmitter(0L);
        sseEmitterManager.subscribe(executionId, emitter);
        return emitter;
    }
}
