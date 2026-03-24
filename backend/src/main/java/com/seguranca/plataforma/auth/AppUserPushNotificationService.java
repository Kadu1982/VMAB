package com.seguranca.plataforma.auth;

import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.model.IncidentStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class AppUserPushNotificationService {
    // Centraliza o push Expo dos perfis internos para nao espalhar integracao externa pelo dominio operacional.

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserPushNotificationService.class);

    private final AppUserPushDeviceRepository appUserPushDeviceRepository;
    private final AppUserRepository appUserRepository;
    private final RestClient restClient;
    private final boolean enabled;

    public AppUserPushNotificationService(
            AppUserPushDeviceRepository appUserPushDeviceRepository,
            AppUserRepository appUserRepository,
            RestClient.Builder restClientBuilder,
            @Value("${vmab.notifications.expo.enabled:true}") boolean enabled,
            @Value("${vmab.notifications.expo.base-url:https://exp.host/--/api/v2/push/send}") String baseUrl
    ) {
        this.appUserPushDeviceRepository = appUserPushDeviceRepository;
        this.appUserRepository = appUserRepository;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
    }

    public void registerDevice(AppUser user, String expoPushToken, String deviceLabel) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String normalizedToken = expoPushToken.trim();

        appUserPushDeviceRepository.findByExpoPushToken(normalizedToken)
                .ifPresentOrElse(
                        device -> {
                            device.refresh(user.getId(), normalizeDeviceLabel(deviceLabel), now);
                            appUserPushDeviceRepository.save(device);
                        },
                        () -> appUserPushDeviceRepository.save(
                                new AppUserPushDevice(
                                        user.getId(),
                                        normalizedToken,
                                        normalizeDeviceLabel(deviceLabel),
                                        now,
                                        now
                                )
                        )
                );
    }

    public void revokeDevice(AppUser user, String expoPushToken) {
        appUserPushDeviceRepository.findByUserIdAndExpoPushToken(user.getId(), expoPushToken.trim())
                .ifPresent(device -> {
                    device.revoke(OffsetDateTime.now(ZoneOffset.UTC));
                    appUserPushDeviceRepository.save(device);
                });
    }

    public void notifyIncidentWorkflowUpdated(Incident incident) {
        if (!enabled) {
            return;
        }

        Set<Long> targetUserIds = new LinkedHashSet<>();
        appUserRepository.findByRoleInAndEnabledTrue(List.of(AppUserRole.ADMIN, AppUserRole.SUPERVISOR))
                .forEach(user -> targetUserIds.add(user.getId()));

        if (incident.getAssignedAgentId() != null) {
            appUserRepository.findByLinkedAgentIdAndRoleAndEnabledTrue(incident.getAssignedAgentId(), AppUserRole.RONDA)
                    .forEach(user -> targetUserIds.add(user.getId()));
        }

        if (targetUserIds.isEmpty()) {
            return;
        }

        List<AppUserPushDevice> devices = appUserPushDeviceRepository.findByUserIdInAndRevokedAtIsNullOrderByUpdatedAtDesc(List.copyOf(targetUserIds));
        if (devices.isEmpty()) {
            return;
        }

        PushMessage message = buildIncidentMessage(incident);
        for (AppUserPushDevice device : devices) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("to", device.getExpoPushToken());
                payload.put("title", message.title());
                payload.put("body", message.body());
                payload.put("sound", "default");
                payload.put("channelId", "vmab-alertas");
                payload.put("data", Map.of(
                        "incidentId", incident.getId(),
                        "status", incident.getStatus().name(),
                        "residentName", incident.getResidentName(),
                        "address", incident.getAddress()
                ));

                restClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception exception) {
                LOGGER.warn("Falha ao enviar push operacional da ocorrencia {} para o token {}", incident.getId(), device.getExpoPushToken(), exception);
            }
        }
    }

    PushMessage buildIncidentMessage(Incident incident) {
        // Fica acessivel ao teste para validar o texto operacional sem depender do cliente HTTP.
        return switch (incident.getStatus()) {
            case OPEN -> new PushMessage("Nova ocorrencia operacional", "Uma nova ocorrencia entrou na fila: " + incident.getResidentName() + ".");
            case DISPATCHED -> new PushMessage("Ocorrencia despachada", "Equipe em deslocamento para " + incident.getAddress() + ".");
            case ON_SITE -> new PushMessage("Equipe no local", "A equipe confirmou chegada em " + incident.getResidentName() + ".");
            case CLOSED -> new PushMessage("Ocorrencia encerrada", "Atendimento finalizado em " + incident.getResidentName() + ".");
        };
    }

    private String normalizeDeviceLabel(String deviceLabel) {
        return StringUtils.hasText(deviceLabel) ? deviceLabel.trim() : null;
    }

    record PushMessage(String title, String body) {
    }
}
