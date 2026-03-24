package com.seguranca.plataforma.operations.service;

import com.seguranca.plataforma.operations.dto.OperationsStreamEventResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class OperationsRealtimeService {
    // Mantem um barramento SSE simples para o painel reagir sem depender apenas de polling.

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((error) -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(new OperationsStreamEventResponse(
                            "CONNECTED",
                            "Session",
                            null,
                            "Canal em tempo real conectado para " + username,
                            OffsetDateTime.now(ZoneOffset.UTC)
                    )));
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }

        return emitter;
    }

    public void publish(String type, String entityName, Long entityId, String description) {
        OperationsStreamEventResponse payload = new OperationsStreamEventResponse(
                type,
                entityName,
                entityId,
                description,
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("operations").data(payload));
                return false;
            } catch (IOException exception) {
                emitter.completeWithError(exception);
                return true;
            }
        });
    }
}
