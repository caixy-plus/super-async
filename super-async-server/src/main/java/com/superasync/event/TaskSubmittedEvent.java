package com.superasync.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 任务提交事件，用于触发即时轮询以降低延迟。
 */
@Getter
public class TaskSubmittedEvent extends ApplicationEvent {
    private final Long taskId;

    public TaskSubmittedEvent(Object source, Long taskId) {
        super(source);
        this.taskId = taskId;
    }
}
