/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.annotation.PostConstruct
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.stereotype.Component
 *  org.springframework.util.ReflectionUtils
 */
package com.superasync.registry;

import com.superasync.annotation.TaskHandler;
import com.superasync.annotation.TaskReceipt;
import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.listener.TaskListener;
import com.superasync.service.TaskExecutor;
import com.superasync.service.TaskReceiptHandler;
import com.superasync.service.impl.TaskDispatcherImpl;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
public class TaskListenerRegistry {
    private static final Logger log = LoggerFactory.getLogger(TaskListenerRegistry.class);
    private final ApplicationContext applicationContext;
    private final TaskDispatcherImpl dispatcher;

    @PostConstruct
    public void registerAll() {
        Map beans = this.applicationContext.getBeansOfType(Object.class);
        for (Object bean : beans.values()) {
            if (bean.getClass().getName().startsWith("org.springframework")) continue;
            this.registerBean(bean);
        }
        log.info("[TaskListenerRegistry] Auto-registration completed");
    }

    private void registerBean(Object bean) {
        TaskListener listener;
        String taskType;
        Class<?> clazz = bean.getClass();
        if (bean instanceof TaskListener && (taskType = this.resolveTaskTypeFromListener(listener = (TaskListener)bean)) != null) {
            this.dispatcher.registerExecutor(taskType, this.wrapListenerExecutor(listener));
            this.dispatcher.registerReceipt(taskType, this.wrapListenerReceipt(listener));
            log.info("[TaskListenerRegistry] Registered TaskListener for taskType={}", (Object)taskType);
        }
        ReflectionUtils.doWithMethods(clazz, method -> {
            TaskReceipt receiptAnn;
            TaskHandler handlerAnn = (TaskHandler)AnnotationUtils.findAnnotation((Method)method, TaskHandler.class);
            if (handlerAnn != null) {
                String handlerType = handlerAnn.value();
                this.dispatcher.registerExecutor(handlerType, this.wrapMethodExecutor(bean, method));
                log.info("[TaskListenerRegistry] Registered @TaskHandler for taskType={}", (Object)handlerType);
            }
            if ((receiptAnn = (TaskReceipt)AnnotationUtils.findAnnotation((Method)method, TaskReceipt.class)) != null) {
                String receiptType = receiptAnn.value();
                this.dispatcher.registerReceipt(receiptType, this.wrapMethodReceipt(bean, method));
                log.info("[TaskListenerRegistry] Registered @TaskReceipt for taskType={}", (Object)receiptType);
            }
        });
    }

    private String resolveTaskTypeFromListener(TaskListener listener) {
        String name = listener.getClass().getSimpleName();
        if (name.endsWith("Listener")) {
            name = name.substring(0, name.length() - "Listener".length());
        }
        return name.isEmpty() ? null : this.toSnakeCase(name).toUpperCase();
    }

    private TaskExecutor wrapListenerExecutor(TaskListener listener) {
        return context -> listener.onExecute(context);
    }

    private TaskReceiptHandler wrapListenerReceipt(final TaskListener listener) {
        return new TaskReceiptHandler(){

            @Override
            public void onSuccess(TaskContext context, TaskResult result) {
                listener.onReceipt(context, result);
            }

            @Override
            public void onFailure(TaskContext context, TaskResult result) {
                listener.onReceipt(context, result);
            }
        };
    }

    private TaskExecutor wrapMethodExecutor(Object bean, Method method) {
        ReflectionUtils.makeAccessible((Method)method);
        return context -> {
            Object result = ReflectionUtils.invokeMethod((Method)method, (Object)bean, (Object[])new Object[]{context});
            if (result instanceof TaskResult) {
                TaskResult tr = (TaskResult)result;
                return tr;
            }
            return TaskResult.ok(result != null ? result.toString() : null);
        };
    }

    private TaskReceiptHandler wrapMethodReceipt(final Object bean, final Method method) {
        ReflectionUtils.makeAccessible((Method)method);
        return new TaskReceiptHandler(){

            @Override
            public void onSuccess(TaskContext context, TaskResult result) {
                ReflectionUtils.invokeMethod((Method)method, (Object)bean, (Object[])new Object[]{context, result});
            }

            @Override
            public void onFailure(TaskContext context, TaskResult result) {
                ReflectionUtils.invokeMethod((Method)method, (Object)bean, (Object[])new Object[]{context, result});
            }
        };
    }

    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2");
    }

    public TaskListenerRegistry(ApplicationContext applicationContext, TaskDispatcherImpl dispatcher) {
        this.applicationContext = applicationContext;
        this.dispatcher = dispatcher;
    }
}

