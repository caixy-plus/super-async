package com.superasync.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SuperAsync Worker HTTP 客户端
 * <p>负责向调度器轮询任务和上报结果。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class SuperAsyncWorkerClient {

    private final SuperAsyncWorkerProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 拉取任务
     */
    @SuppressWarnings("unchecked")
    public WorkerTask poll() {
        String url = properties.getServerUrl() + "/v1/worker/poll";
        Map<String, Object> body = new HashMap<>();
        body.put("workerId", properties.getWorkerId());
        body.put("tags", properties.getTags());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> result = response.getBody();
            if (result == null || !Integer.valueOf(0).equals(result.get("code"))) {
                return null;
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data == null) {
                return null;
            }
            WorkerTask task = new WorkerTask();
            task.setTaskId(((Number) data.get("taskId")).longValue());
            task.setTaskType((String) data.get("taskType"));
            task.setTaskKey((String) data.get("taskKey"));
            task.setPayload((String) data.get("payload"));
            task.setRetryCount(((Number) data.get("retryCount")).intValue());
            task.setMaxRetry(((Number) data.get("maxRetry")).intValue());
            Object execId = data.get("executionId");
            if (execId != null) {
                task.setExecutionId(((Number) execId).longValue());
            }
            return task;
        } catch (Exception e) {
            log.error("[WorkerClient] Poll failed", e);
            return null;
        }
    }

    /**
     * 批量拉取任务
     */
    @SuppressWarnings("unchecked")
    public List<WorkerTask> pollBatch(int batchSize) {
        String url = properties.getServerUrl() + "/v1/worker/pollBatch";
        Map<String, Object> body = new HashMap<>();
        body.put("workerId", properties.getWorkerId());
        body.put("tags", properties.getTags());
        body.put("batchSize", batchSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> result = response.getBody();
            if (result == null || !Integer.valueOf(0).equals(result.get("code"))) {
                return null;
            }
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
            if (dataList == null || dataList.isEmpty()) {
                return null;
            }
            List<WorkerTask> tasks = new ArrayList<>();
            for (Map<String, Object> data : dataList) {
                WorkerTask task = new WorkerTask();
                task.setTaskId(((Number) data.get("taskId")).longValue());
                task.setTaskType((String) data.get("taskType"));
                task.setTaskKey((String) data.get("taskKey"));
                task.setPayload((String) data.get("payload"));
                task.setRetryCount(((Number) data.get("retryCount")).intValue());
                task.setMaxRetry(((Number) data.get("maxRetry")).intValue());
                Object execId = data.get("executionId");
                if (execId != null) {
                    task.setExecutionId(((Number) execId).longValue());
                }
                tasks.add(task);
            }
            return tasks;
        } catch (Exception e) {
            log.error("[WorkerClient] PollBatch failed", e);
            return null;
        }
    }

    /**
     * 上报任务结果
     */
    public void complete(Long taskId, Long executionId, boolean success, String payload, String errorMsg) {
        String url = properties.getServerUrl() + "/v1/worker/complete";
        Map<String, Object> body = new HashMap<>();
        body.put("taskId", taskId);
        body.put("executionId", executionId);
        body.put("success", success);
        body.put("payload", payload);
        body.put("errorMsg", errorMsg);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, Map.class);
        } catch (Exception e) {
            log.error("[WorkerClient] Complete task {} failed", taskId, e);
        }
    }

    /**
     * Worker 任务封装
     */
    @lombok.Data
    public static class WorkerTask {
        private Long taskId;
        private String taskType;
        private String taskKey;
        private String payload;
        private int retryCount;
        private int maxRetry;
        private Long executionId;
    }
}
