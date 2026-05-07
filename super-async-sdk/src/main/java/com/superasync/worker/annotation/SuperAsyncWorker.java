package com.superasync.worker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Worker 任务处理器方法
 * <p>被标记的方法会在 Worker 服务中被自动注册为对应 taskType 的处理器。</p>
 * <p>方法签名要求：</p>
 * <ul>
 *   <li>参数：{@link com.superasync.dto.TaskContext}</li>
 *   <li>返回：{@link com.superasync.dto.TaskResult} 或任意类型（会被包装为 TaskResult）</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuperAsyncWorker {

    /** 任务类型 */
    String value();
}
