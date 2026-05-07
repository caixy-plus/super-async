package com.superasync.client;

import com.superasync.client.dto.ScheduledJobRequest;
import com.superasync.client.dto.TaskSubmitRequest;
import com.superasync.client.dto.TaskSubmitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SuperAsync 客户端
 * <p>业务侧通过此客户端向 SuperAsync 服务端提交异步任务。</p>
 * <p>使用示例：</pre>
 * <pre>
 *   SuperAsyncClient client = new SuperAsyncClient(properties);
 *   Long taskId = client.submit(TaskSubmitRequest.builder()
 *       .taskType("SEND_EMAIL")
 *       .taskKey("email:user123:welcome")
 *       .payload("{\"to\":\"user@example.com\"}")
 *       .build());
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public class SuperAsyncClient {

    private final SuperAsyncProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 提交异步任务
     *
     * @param request 任务请求
     * @return 任务 ID
     */
    public Long submit(TaskSubmitRequest request) {
        String url = properties.getServerUrl() + "/v1/tasks";

        Map<String, Object> body = new HashMap<>();
        body.put("taskType", request.getTaskType());
        body.put("taskKey", request.getTaskKey());
        body.put("payload", request.getPayload());
        body.put("priority", request.getPriority());
        body.put("delaySeconds", request.getDelay().getSeconds());
        body.put("timeoutSeconds", request.getTimeout().getSeconds());
        body.put("maxRetry", request.getMaxRetry());
        body.put("workerTag", request.getWorkerTag());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TaskSubmitResponse> response = restTemplate.postForEntity(url, entity, TaskSubmitResponse.class);
            TaskSubmitResponse result = response.getBody();
            if (result == null || result.getCode() != 0) {
                throw new RuntimeException("提交任务失败: " + (result != null ? result.getMessage() : "空响应"));
            }
            log.info("[SuperAsyncClient] Submitted task id={}, type={}, key={}", result.getData(), request.getTaskType(), request.getTaskKey());
            return result.getData();
        } catch (Exception e) {
            log.error("[SuperAsyncClient] Failed to submit task type={}, key={}", request.getTaskType(), request.getTaskKey(), e);
            throw new RuntimeException("提交异步任务失败", e);
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务 ID
     * @return 任务实体（JSON Map）
     */
    public Map<String, Object> getTask(Long taskId) {
        String url = properties.getServerUrl() + "/v1/tasks/" + taskId;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> result = response.getBody();
            if (result == null) {
                throw new RuntimeException("空响应");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return data;
        } catch (Exception e) {
            log.error("[SuperAsyncClient] Failed to get task id={}", taskId, e);
            throw new RuntimeException("查询任务失败", e);
        }
    }

    /**
     * 根据业务幂等键查询任务
     *
     * @param taskKey 业务幂等键
     * @return 任务实体（JSON Map）
     */
    public Map<String, Object> getTaskByKey(String taskKey) {
        String url = properties.getServerUrl() + "/v1/tasks/key/" + taskKey;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> result = response.getBody();
            if (result == null) {
                throw new RuntimeException("空响应");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return data;
        } catch (Exception e) {
            log.error("[SuperAsyncClient] Failed to get task key={}", taskKey, e);
            throw new RuntimeException("查询任务失败", e);
        }
    }

    /**
     * 注册或更新定时任务
     *
     * @param request 定时任务请求
     * @return 任务定义 ID
     */
    public Long registerScheduledJob(ScheduledJobRequest request) {
        String url = properties.getServerUrl() + "/v1/scheduled-jobs";

        Map<String, Object> body = new HashMap<>();
        body.put("jobName", request.getJobName());
        body.put("taskType", request.getTaskType());
        body.put("taskKey", request.getTaskKey());
        body.put("payload", request.getPayload());
        body.put("cronExpression", request.getCronExpression());
        body.put("workerTag", request.getWorkerTag());
        body.put("description", request.getDescription());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> result = response.getBody();
            if (result == null || !Integer.valueOf(0).equals(result.get("code"))) {
                throw new RuntimeException("注册定时任务失败: " + (result != null ? result.get("message") : "空响应"));
            }
            @SuppressWarnings("unchecked")
            Long jobId = ((Number) result.get("data")).longValue();
            log.info("[SuperAsyncClient] Registered scheduled job id={}, name={}", jobId, request.getJobName());
            return jobId;
        } catch (Exception e) {
            log.error("[SuperAsyncClient] Failed to register scheduled job name={}", request.getJobName(), e);
            throw new RuntimeException("注册定时任务失败", e);
        }
    }

    /**
     * 查询定时任务列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listScheduledJobs() {
        String url = properties.getServerUrl() + "/v1/scheduled-jobs";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> result = response.getBody();
            if (result == null) {
                throw new RuntimeException("空响应");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data == null) {
                return List.of();
            }
            return (List<Map<String, Object>>) data.get("content");
        } catch (Exception e) {
            log.error("[SuperAsyncClient] Failed to list scheduled jobs", e);
            throw new RuntimeException("查询定时任务失败", e);
        }
    }
}
