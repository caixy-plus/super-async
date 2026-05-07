package com.superasync.engine;

import com.superasync.event.TaskSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 任务提交后的即时轮询器。
 * <p>任务落库后立即触发一次 pollAndDispatch，缓解硬编码轮询间隔带来的延迟 floor。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEagerPoller {

    private final TaskPollingScheduler scheduler;

    @Async
    @EventListener
    public void onTaskSubmitted(TaskSubmittedEvent event) {
        try {
            scheduler.pollAndDispatch();
        } catch (Exception e) {
            log.debug("[EagerPoller] 立即轮询失败，等待下一个定时周期充底", e);
        }
    }
}
