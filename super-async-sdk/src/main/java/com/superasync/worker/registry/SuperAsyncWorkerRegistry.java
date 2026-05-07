package com.superasync.worker.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.worker.annotation.SuperAsyncWorker;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Worker 处理器注册表
 * <p>扫描所有 Bean 中的 {@link SuperAsyncWorker} 注解方法并注册。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAsyncWorkerRegistry {

    private final ApplicationContext applicationContext;

    @Getter
    private final Map<String, HandlerHolder> handlers = new ConcurrentHashMap<>();

    @PostConstruct
    public void registerAll() {
        Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);
        for (Object bean : beans.values()) {
            if (bean.getClass().getName().startsWith("org.springframework")) {
                continue;
            }
            registerBean(bean);
        }
        log.info("[WorkerRegistry] Registered {} handlers", handlers.size());
    }

    private void registerBean(Object bean) {
        Class<?> clazz = bean.getClass();
        ReflectionUtils.doWithMethods(clazz, method -> {
            SuperAsyncWorker ann = AnnotationUtils.findAnnotation(method, SuperAsyncWorker.class);
            if (ann != null) {
                String taskType = ann.value();
                handlers.put(taskType, new HandlerHolder(bean, method));
                log.info("[WorkerRegistry] Registered handler for taskType={}", taskType);
            }
        });
    }

    public TaskResult execute(String taskType, TaskContext context) {
        HandlerHolder holder = handlers.get(taskType);
        if (holder == null) {
            throw new RuntimeException("No handler registered for taskType: " + taskType);
        }
        ReflectionUtils.makeAccessible(holder.method());
        Object arg = adaptArgument(holder.method(), context);
        Object result = ReflectionUtils.invokeMethod(holder.method(), holder.bean(), arg);
        if (result instanceof TaskResult tr) {
            return tr;
        }
        return TaskResult.ok(result != null ? result.toString() : null);
    }

    private Object adaptArgument(Method method, TaskContext context) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return null;
        }
        Class<?> paramType = paramTypes[0];
        if (paramType == TaskContext.class) {
            return context;
        }
        if (paramType == String.class) {
            return context.getPayload();
        }
        if (Map.class.isAssignableFrom(paramType)) {
            try {
                return new ObjectMapper().readValue(context.getPayload(), Map.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse payload as Map: " + context.getPayload(), e);
            }
        }
        return context;
    }

    public boolean hasHandler(String taskType) {
        return handlers.containsKey(taskType);
    }

    public record HandlerHolder(Object bean, Method method) {
    }
}
