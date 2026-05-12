/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.transaction.annotation.Transactional
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.superasync.controller;

import com.superasync.dto.Result;
import com.superasync.entity.AsyncTaskEntity;
import com.superasync.repository.AsyncTaskRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/v1/worker"})
public class WorkerController {
    private static final Logger log = LoggerFactory.getLogger(WorkerController.class);
    private final AsyncTaskRepository taskRepository;
    private final com.superasync.service.JobExecutionService executionService;

    @PostMapping(value={"/poll"})
    @Transactional
    public Result<WorkerTask> poll(@RequestBody PollRequest request) {
        if (request.getTags() == null || request.getTags().isEmpty()) {
            return Result.error(400, "tags \u4e0d\u80fd\u4e3a\u7a7a");
        }
        AsyncTaskEntity task = null;
        for (String tag : request.getTags()) {
            task = this.taskRepository.pollWorkerTask(OffsetDateTime.now(), tag);
            if (task == null) continue;
            break;
        }
        if (task == null) {
            return Result.success(null);
        }
        this.taskRepository.lockTask(task.getId(), request.getWorkerId());
        WorkerTask wt = new WorkerTask();
        wt.setTaskId(task.getId());
        wt.setTaskType(task.getTaskType());
        wt.setTaskKey(task.getTaskKey());
        wt.setPayload(task.getPayload());
        wt.setRetryCount(task.getRetryCount());
        wt.setMaxRetry(task.getMaxRetry());
        wt.setExecutionId(task.getExecutionId());
        return Result.success(wt);
    }

    @PostMapping(value={"/pollBatch"})
    @Transactional
    public Result<List<WorkerTask>> pollBatch(@RequestBody PollRequest request) {
        if (request.getTags() == null || request.getTags().isEmpty()) {
            return Result.error(400, "tags 不能为空");
        }
        int batchSize = request.getBatchSize() != null ? request.getBatchSize() : 10;
        List<WorkerTask> result = new ArrayList<>();
        for (String tag : request.getTags()) {
            List<AsyncTaskEntity> tasks = this.taskRepository.pollWorkerTasks(OffsetDateTime.now(), tag, batchSize);
            for (AsyncTaskEntity task : tasks) {
                this.taskRepository.lockTask(task.getId(), request.getWorkerId());
                WorkerTask wt = new WorkerTask();
                wt.setTaskId(task.getId());
                wt.setTaskType(task.getTaskType());
                wt.setTaskKey(task.getTaskKey());
                wt.setPayload(task.getPayload());
                wt.setRetryCount(task.getRetryCount());
                wt.setMaxRetry(task.getMaxRetry());
                wt.setExecutionId(task.getExecutionId());
                result.add(wt);
            }
            if (!result.isEmpty()) break;
        }
        return Result.success(result);
    }

    @PostMapping(value={"/appendLog"})
    @Transactional
    public Result<Void> appendLog(@RequestBody AppendLogRequest request) {
        if (request.getExecutionId() == null) {
            return Result.error(400, "executionId 不能为空");
        }
        executionService.appendLog(request.getExecutionId(), request.getLevel(), request.getMessage());
        return Result.success();
    }

    @PostMapping(value={"/appendLogBatch"})
    @Transactional
    public Result<Void> appendLogBatch(@RequestBody List<AppendLogRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Result.success();
        }
        for (AppendLogRequest request : requests) {
            if (request.getExecutionId() == null) {
                continue;
            }
            executionService.appendLog(request.getExecutionId(), request.getLevel(), request.getMessage());
        }
        return Result.success();
    }

    @PostMapping(value={"/complete"})
    @Transactional
    public Result<Void> complete(@RequestBody CompleteRequest request) {
        AsyncTaskEntity task = this.taskRepository.findByTaskId(request.getTaskId());
        if (task == null) {
            return Result.error(404, "\u4efb\u52a1\u4e0d\u5b58\u5728");
        }
        Long executionId = request.getExecutionId();
        if (request.isSuccess()) {
            this.taskRepository.completeTask(task.getId(), "SUCCESS", request.getPayload(), null);
            log.info("[WorkerController] Task {} completed by worker", (Object)request.getTaskId());
            if (executionId != null) {
                executionService.markCompleted(executionId, true, null);
                executionService.appendLog(executionId, "INFO", String.format("[Worker] Task id=%d completed", request.getTaskId()));
            }
        } else if (task.getRetryCount() < task.getMaxRetry()) {
            this.taskRepository.markForRetry(task.getId(), OffsetDateTime.now().plusSeconds(10L));
            log.warn("[WorkerController] Task {} failed, scheduled retry {}/{}", new Object[]{request.getTaskId(), task.getRetryCount() + 1, task.getMaxRetry()});
            if (executionId != null) {
                executionService.appendLog(executionId, "WARN", String.format("[Worker] Task id=%d failed, scheduled retry %d/%d", request.getTaskId(), task.getRetryCount() + 1, task.getMaxRetry()));
            }
        } else {
            this.taskRepository.completeTask(task.getId(), "FAIL", request.getPayload(), request.getErrorMsg());
            log.error("[WorkerController] Task {} failed after all retries", (Object)request.getTaskId());
            if (executionId != null) {
                executionService.markCompleted(executionId, false, request.getErrorMsg());
                executionService.appendLog(executionId, "ERROR", String.format("[Worker] Task id=%d failed after all retries: %s", request.getTaskId(), request.getErrorMsg()));
            }
        }
        return Result.success();
    }

    public WorkerController(AsyncTaskRepository taskRepository, com.superasync.service.JobExecutionService executionService) {
        this.taskRepository = taskRepository;
        this.executionService = executionService;
    }

    public static class PollRequest {
        private String workerId;
        private List<String> tags;
        private Integer batchSize;

        public String getWorkerId() {
            return this.workerId;
        }

        public List<String> getTags() {
            return this.tags;
        }

        public Integer getBatchSize() {
            return this.batchSize;
        }

        public void setWorkerId(String workerId) {
            this.workerId = workerId;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public void setBatchSize(Integer batchSize) {
            this.batchSize = batchSize;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof PollRequest)) {
                return false;
            }
            PollRequest other = (PollRequest)o;
            if (!other.canEqual(this)) {
                return false;
            }
            String this$workerId = this.getWorkerId();
            String other$workerId = other.getWorkerId();
            if (this$workerId == null ? other$workerId != null : !this$workerId.equals(other$workerId)) {
                return false;
            }
            List<String> this$tags = this.getTags();
            List<String> other$tags = other.getTags();
            return !(this$tags == null ? other$tags != null : !((Object)this$tags).equals(other$tags));
        }

        protected boolean canEqual(Object other) {
            return other instanceof PollRequest;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            String $workerId = this.getWorkerId();
            result = result * 59 + ($workerId == null ? 43 : $workerId.hashCode());
            List<String> $tags = this.getTags();
            result = result * 59 + ($tags == null ? 43 : ((Object)$tags).hashCode());
            return result;
        }

        public String toString() {
            return "WorkerController.PollRequest(workerId=" + this.getWorkerId() + ", tags=" + String.valueOf(this.getTags()) + ")";
        }
    }

    public static class WorkerTask {
        private Long taskId;
        private String taskType;
        private String taskKey;
        private String payload;
        private int retryCount;
        private int maxRetry;
        private Long executionId;

        public Long getTaskId() {
            return this.taskId;
        }

        public String getTaskType() {
            return this.taskType;
        }

        public String getTaskKey() {
            return this.taskKey;
        }

        public String getPayload() {
            return this.payload;
        }

        public int getRetryCount() {
            return this.retryCount;
        }

        public int getMaxRetry() {
            return this.maxRetry;
        }

        public Long getExecutionId() {
            return this.executionId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public void setTaskType(String taskType) {
            this.taskType = taskType;
        }

        public void setTaskKey(String taskKey) {
            this.taskKey = taskKey;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        public void setMaxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        public void setExecutionId(Long executionId) {
            this.executionId = executionId;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof WorkerTask)) {
                return false;
            }
            WorkerTask other = (WorkerTask)o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (this.getRetryCount() != other.getRetryCount()) {
                return false;
            }
            if (this.getMaxRetry() != other.getMaxRetry()) {
                return false;
            }
            Long this$executionId = this.getExecutionId();
            Long other$executionId = other.getExecutionId();
            if (this$executionId == null ? other$executionId != null : !((Object)this$executionId).equals(other$executionId)) {
                return false;
            }
            Long this$taskId = this.getTaskId();
            Long other$taskId = other.getTaskId();
            if (this$taskId == null ? other$taskId != null : !((Object)this$taskId).equals(other$taskId)) {
                return false;
            }
            String this$taskType = this.getTaskType();
            String other$taskType = other.getTaskType();
            if (this$taskType == null ? other$taskType != null : !this$taskType.equals(other$taskType)) {
                return false;
            }
            String this$taskKey = this.getTaskKey();
            String other$taskKey = other.getTaskKey();
            if (this$taskKey == null ? other$taskKey != null : !this$taskKey.equals(other$taskKey)) {
                return false;
            }
            String this$payload = this.getPayload();
            String other$payload = other.getPayload();
            return !(this$payload == null ? other$payload != null : !this$payload.equals(other$payload));
        }

        protected boolean canEqual(Object other) {
            return other instanceof WorkerTask;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * 59 + this.getRetryCount();
            result = result * 59 + this.getMaxRetry();
            Long $executionId = this.getExecutionId();
            result = result * 59 + ($executionId == null ? 43 : ((Object)$executionId).hashCode());
            Long $taskId = this.getTaskId();
            result = result * 59 + ($taskId == null ? 43 : ((Object)$taskId).hashCode());
            String $taskType = this.getTaskType();
            result = result * 59 + ($taskType == null ? 43 : $taskType.hashCode());
            String $taskKey = this.getTaskKey();
            result = result * 59 + ($taskKey == null ? 43 : $taskKey.hashCode());
            String $payload = this.getPayload();
            result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
            return result;
        }

        public String toString() {
            return "WorkerController.WorkerTask(taskId=" + this.getTaskId() + ", taskType=" + this.getTaskType() + ", taskKey=" + this.getTaskKey() + ", payload=" + this.getPayload() + ", retryCount=" + this.getRetryCount() + ", maxRetry=" + this.getMaxRetry() + ", executionId=" + this.getExecutionId() + ")";
        }
    }

    public static class AppendLogRequest {
        private Long executionId;
        private String level;
        private String message;

        public Long getExecutionId() {
            return this.executionId;
        }

        public String getLevel() {
            return this.level;
        }

        public String getMessage() {
            return this.message;
        }

        public void setExecutionId(Long executionId) {
            this.executionId = executionId;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class CompleteRequest {
        private Long taskId;
        private boolean success;
        private String payload;
        private String errorMsg;
        private Long executionId;

        public Long getTaskId() {
            return this.taskId;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public String getPayload() {
            return this.payload;
        }

        public String getErrorMsg() {
            return this.errorMsg;
        }

        public Long getExecutionId() {
            return this.executionId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public void setExecutionId(Long executionId) {
            this.executionId = executionId;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof CompleteRequest)) {
                return false;
            }
            CompleteRequest other = (CompleteRequest)o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (this.isSuccess() != other.isSuccess()) {
                return false;
            }
            Long this$taskId = this.getTaskId();
            Long other$taskId = other.getTaskId();
            if (this$taskId == null ? other$taskId != null : !((Object)this$taskId).equals(other$taskId)) {
                return false;
            }
            Long this$executionId = this.getExecutionId();
            Long other$executionId = other.getExecutionId();
            if (this$executionId == null ? other$executionId != null : !((Object)this$executionId).equals(other$executionId)) {
                return false;
            }
            String this$payload = this.getPayload();
            String other$payload = other.getPayload();
            if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload)) {
                return false;
            }
            String this$errorMsg = this.getErrorMsg();
            String other$errorMsg = other.getErrorMsg();
            return !(this$errorMsg == null ? other$errorMsg != null : !this$errorMsg.equals(other$errorMsg));
        }

        protected boolean canEqual(Object other) {
            return other instanceof CompleteRequest;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * 59 + (this.isSuccess() ? 79 : 97);
            Long $taskId = this.getTaskId();
            result = result * 59 + ($taskId == null ? 43 : ((Object)$taskId).hashCode());
            Long $executionId = this.getExecutionId();
            result = result * 59 + ($executionId == null ? 43 : ((Object)$executionId).hashCode());
            String $payload = this.getPayload();
            result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
            String $errorMsg = this.getErrorMsg();
            result = result * 59 + ($errorMsg == null ? 43 : $errorMsg.hashCode());
            return result;
        }

        public String toString() {
            return "WorkerController.CompleteRequest(taskId=" + this.getTaskId() + ", success=" + this.isSuccess() + ", payload=" + this.getPayload() + ", errorMsg=" + this.getErrorMsg() + ", executionId=" + this.getExecutionId() + ")";
        }
    }
}

