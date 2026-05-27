package com.seguranca.plataforma.operations.residentapp.service;

import com.seguranca.plataforma.operations.residentapp.model.ResidentAlert;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertStatus;
import com.seguranca.plataforma.operations.residentapp.model.ResidentPushDevice;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentPushDeviceRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ResidentPushNotificationService {
    // Centraliza o envio de push do morador para que a regra de alerta não espalhe detalhes da Expo.

    private static final Logger LOGGER = LoggerFactory.getLogger(ResidentPushNotificationService.class);

    private final ResidentPushDeviceRepository residentPushDeviceRepository;
    private final RestClient restClient;
    private final boolean enabled;

    public ResidentPushNotificationService(
            ResidentPushDeviceRepository residentPushDeviceRepository,
            RestClient.Builder restClientBuilder,
            @Value("${vmab.notifications.expo.enabled:true}") boolean enabled,
            @Value("${vmab.notifications.expo.base-url:https://exp.host/--/api/v2/push/send}") String baseUrl
    ) {
        this.residentPushDeviceRepository = residentPushDeviceRepository;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
    }

    public void notifyResidentAlertStatusChanged(ResidentAlert alert) {
        if (!enabled) {
            return;
        }

        List<ResidentPushDevice> devices = residentPushDeviceRepository.findByResidentIdAndRevokedAtIsNullOrderByUpdatedAtDesc(alert.getResidentId());
        if (devices.isEmpty()) {
            return;
        }

        PushMessage message = buildMessage(alert);
        for (ResidentPushDevice device : devices) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("to", device.getExpoPushToken());
                payload.put("title", message.title());
                payload.put("body", message.body());
                payload.put("sound", "default");
                payload.put("channelId", "vmab-alertas");
                payload.put("data", Map.of(
                        "residentAlertId", alert.getId(),
                        "residentId", alert.getResidentId(),
                        "status", alert.getStatus().name(),
                        "type", alert.getType().name()
                ));

                restClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception exception) {
                LOGGER.warn("Falha ao enviar push Expo do alerta {} para o token {}", alert.getId(), device.getExpoPushToken(), exception);
            }
        }
    }

    PushMessage buildMessage(ResidentAlert alert) {
        // Fica visivel no pacote para que os testes validem a mensagem sem acoplar a regra ao cliente HTTP.
        if (alert.isSilent()) {
            return switch (alert.getStatus()) {
                case ACKNOWLEDGED -> new PushMessage("Atualizacao de atendimento", "Sua solicitacao silenciosa foi recebida pela central.");
                case DISPATCHED -> new PushMessage("Atualizacao de atendimento", "A equipe ja esta a caminho.");
                case ON_SITE -> new PushMessage("Atualizacao de atendimento", "A equipe chegou ao local.");
                case RESOLVED -> new PushMessage("Atualizacao de atendimento", "O atendimento foi encerrado.");
                case CANCELLED -> new PushMessage("Atualizacao de atendimento", "A solicitacao foi cancelada.");
                default -> new PushMessage("Atualizacao de atendimento", "Houve uma atualizacao importante no seu atendimento.");
            };
        }

        return switch (alert.getStatus()) {
            case ACKNOWLEDGED -> new PushMessage("Alerta recebido", "A central recebeu seu alerta e iniciou o atendimento.");
            case DISPATCHED -> new PushMessage("Equipe em deslocamento", "A equipe foi despachada para o seu atendimento.");
            case ON_SITE -> new PushMessage("Equipe no local", "A equipe ja chegou ao local informado.");
            case RESOLVED -> new PushMessage("Atendimento encerrado", "Seu alerta foi finalizado pela operação.");
            case CANCELLED -> new PushMessage("Alerta cancelado", "Seu alerta foi cancelado.");
            default -> new PushMessage("Atualizacao de alerta", "Seu alerta recebeu uma atualizacao operacional.");
        };
    }

    record PushMessage(String title, String body) {
    }
}


