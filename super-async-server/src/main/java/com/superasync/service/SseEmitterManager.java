package com.superasync.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {
    private final ConcurrentHashMap<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void subscribe(Long executionId, SseEmitter emitter) {
        emitters.computeIfAbsent(executionId, k -> Collections.synchronizedList(new ArrayList<>())).add(emitter);
        emitter.onCompletion(() -> remove(executionId, emitter));
        emitter.onTimeout(() -> remove(executionId, emitter));
        emitter.onError(e -> remove(executionId, emitter));
    }

    public void send(Long executionId, String level, String message) {
        List<SseEmitter> list = emitters.get(executionId);
        if (list == null) return;
        String data = String.format("[%s] %s", level, message);
        list.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(data));
                return false;
            } catch (Exception e) {
                return true;
            }
        });
    }

    private void remove(Long executionId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(executionId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(executionId);
            }
        }
    }
}
