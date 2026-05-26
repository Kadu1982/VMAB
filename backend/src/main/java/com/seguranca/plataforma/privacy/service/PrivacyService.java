package com.seguranca.plataforma.privacy.service;

import com.seguranca.plataforma.auth.AppUser;
import com.seguranca.plataforma.auth.AppUserPushDeviceRepository;
import com.seguranca.plataforma.auth.AppUserRepository;
import com.seguranca.plataforma.auth.PasswordResetTokenRepository;
import com.seguranca.plataforma.config.VmabRetentionProperties;
import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.operations.repository.ShiftRepository;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlert;
import com.seguranca.plataforma.operations.residentapp.model.ResidentPushDevice;
import com.seguranca.plataforma.operations.residentapp.model.ResidentSession;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentAlertRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentPushDeviceRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentSessionRepository;
import com.seguranca.plataforma.privacy.dto.CreatePrivacyRequestRequest;
import com.seguranca.plataforma.privacy.dto.PrivacyExportResponse;
import com.seguranca.plataforma.privacy.dto.PrivacyRetentionStatusResponse;
import com.seguranca.plataforma.privacy.dto.PrivacyRequestResponse;
import com.seguranca.plataforma.privacy.dto.UpdatePrivacyRequestRequest;
import com.seguranca.plataforma.privacy.model.PrivacyRequest;
import com.seguranca.plataforma.privacy.model.PrivacyRequestStatus;
import com.seguranca.plataforma.privacy.model.PrivacyRequestType;
import com.seguranca.plataforma.privacy.model.PrivacySubjectType;
import com.seguranca.plataforma.privacy.repository.PrivacyRequestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PrivacyService {
    // Consolida pedidos LGPD sem misturar esses fluxos com a operacao normal da plataforma.

    private final PrivacyRequestRepository privacyRequestRepository;
    private final ResidentRepository residentRepository;
    private final ResidentAlertRepository residentAlertRepository;
    private final ResidentSessionRepository residentSessionRepository;
    private final VmabRetentionProperties retentionProperties;
    private final AppUserRepository appUserRepository;
    private final AppUserPushDeviceRepository appUserPushDeviceRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AgentRepository agentRepository;
    private final ShiftRepository shiftRepository;
    private final ResidentPushDeviceRepository residentPushDeviceRepository;
    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;

    public PrivacyService(
            PrivacyRequestRepository privacyRequestRepository,
            ResidentRepository residentRepository,
            ResidentAlertRepository residentAlertRepository,
            ResidentSessionRepository residentSessionRepository,
            VmabRetentionProperties retentionProperties,
            AppUserRepository appUserRepository,
            AppUserPushDeviceRepository appUserPushDeviceRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AgentRepository agentRepository,
            ShiftRepository shiftRepository,
            ResidentPushDeviceRepository residentPushDeviceRepository,
            AuditRecordRepository auditRecordRepository,
            ObjectMapper objectMapper
    ) {
        this.privacyRequestRepository = privacyRequestRepository;
        this.residentRepository = residentRepository;
        this.residentAlertRepository = residentAlertRepository;
        this.residentSessionRepository = residentSessionRepository;
        this.retentionProperties = retentionProperties;
        this.appUserRepository = appUserRepository;
        this.appUserPushDeviceRepository = appUserPushDeviceRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.agentRepository = agentRepository;
        this.shiftRepository = shiftRepository;
        this.residentPushDeviceRepository = residentPushDeviceRepository;
        this.auditRecordRepository = auditRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<PrivacyRequestResponse> listRequests() {
        return privacyRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                .map(PrivacyRequestResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PrivacyRequestResponse createRequest(CreatePrivacyRequestRequest request) {
        SubjectSnapshot snapshot = resolveSubjectSnapshot(request.subjectType(), request.subjectId());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        PrivacyRequest privacyRequest = new PrivacyRequest(
                request.requestType(),
                request.subjectType(),
                snapshot.subjectId(),
                snapshot.subjectLabel(),
                PrivacyRequestStatus.OPEN,
                resolveCurrentActorUsername(),
                now,
                normalizeNotes(request.notes())
        );

        PrivacyRequest savedRequest = privacyRequestRepository.save(privacyRequest);
        recordAudit(AuditActionType.CREATE, "PrivacyRequest", savedRequest.getId(), "Pedido LGPD criado para " + snapshot.subjectLabel());
        return PrivacyRequestResponse.fromEntity(savedRequest);
    }

    @Transactional
    public PrivacyRequestResponse updateRequest(Long id, UpdatePrivacyRequestRequest request) {
        PrivacyRequest privacyRequest = getRequest(id);
        if (privacyRequest.getStatus() == PrivacyRequestStatus.COMPLETED || privacyRequest.getStatus() == PrivacyRequestStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido LGPD ja foi encerrado.");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        privacyRequest.updateStatus(request.status(), resolveCurrentActorUsername(), now, normalizeNotes(request.notes()));
        if (request.notifySubject()) {
            privacyRequest.registerSubjectNotification(
                    normalizeNotes(request.notificationChannel()),
                    normalizeNotes(request.notificationNotes()),
                    now
            );
        }
        if (request.status() == PrivacyRequestStatus.COMPLETED) {
            applyCompletionEffects(privacyRequest, now);
        }
        PrivacyRequest savedRequest = privacyRequestRepository.save(privacyRequest);
        recordAudit(AuditActionType.UPDATE, "PrivacyRequest", savedRequest.getId(), "Pedido LGPD atualizado para " + savedRequest.getStatus());
        return PrivacyRequestResponse.fromEntity(savedRequest);
    }

    @Transactional
    public PrivacyExportResponse exportSubject(PrivacySubjectType subjectType, Long subjectId) {
        // Gera uma exportacao objetiva e controlada, sem vazar hashes ou segredos internos.
        SubjectSnapshot snapshot = resolveSubjectSnapshot(subjectType, subjectId);
        Map<String, Object> payload = switch (subjectType) {
            case RESIDENT -> buildResidentExport(subjectId);
            case APP_USER -> buildAppUserExport(subjectId);
            case AGENT -> buildAgentExport(subjectId);
        };

        privacyRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                .filter(request -> request.getRequestType() == PrivacyRequestType.EXPORT)
                .filter(request -> request.getSubjectType() == subjectType && request.getSubjectId().equals(subjectId))
                .findFirst()
                .ifPresent(request -> request.markExportGenerated(OffsetDateTime.now(ZoneOffset.UTC)));

        recordAudit(AuditActionType.UPDATE, "PrivacyExport", subjectId, "Exportacao de dados gerada para " + snapshot.subjectLabel());
        return new PrivacyExportResponse(subjectType, subjectId, snapshot.subjectLabel(), OffsetDateTime.now(ZoneOffset.UTC), payload);
    }

    @Transactional
    public byte[] exportSubjectAsJson(PrivacySubjectType subjectType, Long subjectId) {
        // Gera um arquivo transportavel para entrega formal do pedido LGPD.
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportSubject(subjectType, subjectId));
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nao foi possivel gerar o arquivo de exportacao.");
        }
    }

    @Transactional(readOnly = true)
    public String buildNotificationDraft(Long requestId) {
        // Gera um comunicado padrao para o titular sem depender de integracao externa de envio.
        PrivacyRequest request = getRequest(requestId);
        return """
                VMAB - Comunicacao de privacidade

                Titular: %s
                Tipo do pedido: %s
                Status atual: %s
                Solicitado em: %s
                Tratado por: %s
                Tratado em: %s
                Canal registrado: %s

                Observacoes operacionais:
                %s

                Observacoes da notificacao:
                %s

                Este documento foi gerado para apoiar a comunicacao formal do atendimento ao titular dentro do fluxo LGPD da plataforma VMAB.
                """
                .formatted(
                        request.getSubjectLabel(),
                        request.getRequestType().name(),
                        request.getStatus().name(),
                        request.getRequestedAt(),
                        request.getHandledBy() != null ? request.getHandledBy() : "nao definido",
                        request.getHandledAt() != null ? request.getHandledAt() : "nao definido",
                        request.getSubjectNotificationChannel() != null ? request.getSubjectNotificationChannel() : "nao registrado",
                        request.getNotes() != null ? request.getNotes() : "Sem observacoes.",
                        request.getSubjectNotificationNotes() != null ? request.getSubjectNotificationNotes() : "Sem observacoes."
                );
    }

    @Transactional(readOnly = true)
    public PrivacyRetentionStatusResponse retentionStatus() {
        // Exibe o estado operacional da retencao e o ultimo ciclo efetivamente auditado.
        AuditRecord lastCleanup = auditRecordRepository.findTopByEntityNameOrderByOccurredAtDesc("RetentionCleanup").orElse(null);

        return new PrivacyRetentionStatusResponse(
                retentionProperties.isEnabled(),
                retentionProperties.getCleanupCron(),
                retentionProperties.getPasswordResetTokenRetentionHours(),
                retentionProperties.getResidentSessionRetentionDays(),
                retentionProperties.getIncidentEvidenceRetentionDays(),
                retentionProperties.getVehicleMaintenanceRetentionDays(),
                retentionProperties.getHrAttendanceRetentionDays(),
                retentionProperties.isRemoveOrphanEvidenceFiles(),
                privacyRequestRepository.countByStatus(PrivacyRequestStatus.OPEN),
                privacyRequestRepository.countByStatus(PrivacyRequestStatus.IN_PROGRESS),
                privacyRequestRepository.countByStatus(PrivacyRequestStatus.COMPLETED),
                lastCleanup != null ? lastCleanup.getOccurredAt() : null,
                lastCleanup != null ? lastCleanup.getDescription() : null
        );
    }

    private Map<String, Object> buildResidentExport(Long residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador nao encontrado."));
        List<ResidentAlert> alerts = residentAlertRepository.findByResidentIdOrderByOpenedAtDesc(residentId);
        List<ResidentSession> sessions = residentSessionRepository.findByResidentIdOrderByCreatedAtDesc(residentId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("resident", residentSnapshot(resident));
        payload.put("alerts", alerts.stream().map(this::residentAlertSnapshot).toList());
        payload.put("sessions", sessions.stream().map(this::residentSessionSnapshot).toList());
        payload.put("pushDevices", residentPushDeviceRepository.findByResidentIdOrderByUpdatedAtDesc(residentId)
                .stream()
                .map(this::residentPushDeviceSnapshot)
                .toList());
        return payload;
    }

    private Map<String, Object> buildAppUserExport(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", appUserSnapshot(user));
        payload.put("activePasswordResetTokens", passwordResetTokenRepository.findByUserIdAndConsumedAtIsNull(user.getId())
                .stream()
                .map(token -> {
                    Map<String, Object> tokenSnapshot = new LinkedHashMap<>();
                    tokenSnapshot.put("createdAt", token.getCreatedAt());
                    tokenSnapshot.put("expiresAt", token.getExpiresAt());
                    tokenSnapshot.put("consumedAt", token.getConsumedAt());
                    return tokenSnapshot;
                })
                .toList());
        payload.put("pushDevices", appUserPushDeviceRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(device -> {
                    Map<String, Object> snapshot = new LinkedHashMap<>();
                    snapshot.put("id", device.getId());
                    snapshot.put("deviceLabel", device.getDeviceLabel());
                    snapshot.put("createdAt", device.getCreatedAt());
                    snapshot.put("updatedAt", device.getUpdatedAt());
                    snapshot.put("revokedAt", device.getRevokedAt());
                    return snapshot;
                })
                .toList());
        return payload;
    }

    private Map<String, Object> buildAgentExport(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente nao encontrado."));

        long shiftCount = shiftRepository.findAll().stream()
                .filter(shift -> agentId.equals(shift.getAgentId()))
                .count();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("agent", agentSnapshot(agent));
        payload.put("shiftCount", shiftCount);
        return payload;
    }

    private SubjectSnapshot resolveSubjectSnapshot(PrivacySubjectType subjectType, Long subjectId) {
        return switch (subjectType) {
            case RESIDENT -> residentRepository.findById(subjectId)
                    .map(resident -> new SubjectSnapshot(subjectId, resident.getFullName()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador nao encontrado."));
            case APP_USER -> appUserRepository.findById(subjectId)
                    .map(user -> new SubjectSnapshot(subjectId, user.getUsername()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
            case AGENT -> agentRepository.findById(subjectId)
                    .map(agent -> new SubjectSnapshot(subjectId, agent.getFullName()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente nao encontrado."));
        };
    }

    private PrivacyRequest getRequest(Long id) {
        return privacyRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido LGPD nao encontrado."));
    }

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }

    private void applyCompletionEffects(PrivacyRequest privacyRequest, OffsetDateTime now) {
        // Ao concluir o pedido, o sistema registra o efeito material aplicado ao titular.
        if (privacyRequest.getRequestType() == PrivacyRequestType.EXPORT) {
            privacyRequest.markExportGenerated(now);
            return;
        }

        switch (privacyRequest.getSubjectType()) {
            case RESIDENT -> anonymizeResident(privacyRequest.getSubjectId(), now);
            case APP_USER -> anonymizeAppUser(privacyRequest.getSubjectId());
            case AGENT -> anonymizeAgent(privacyRequest.getSubjectId());
        }
        privacyRequest.markDeletionApplied(now);
    }

    private void anonymizeResident(Long residentId, OffsetDateTime now) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador nao encontrado."));
        resident.update(
                "Morador removido #" + residentId,
                "anon-resident-" + residentId,
                "Endereco anonimizado",
                null,
                com.seguranca.plataforma.operations.model.ResidentStatus.INACTIVE,
                null,
                null
        );
        residentRepository.save(resident);

        residentSessionRepository.findByResidentIdOrderByCreatedAtDesc(residentId)
                .forEach(session -> session.revoke(now));
        residentPushDeviceRepository.findByResidentIdOrderByUpdatedAtDesc(residentId)
                .forEach(device -> device.revoke(now));
        residentAlertRepository.findByResidentIdOrderByOpenedAtDesc(residentId)
                .forEach(alert -> alert.anonymizeResidentData("Morador removido #" + residentId, "anon-resident-" + residentId, "Endereco anonimizado"));
    }

    private void anonymizeAppUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
        user.update("anon-user-" + userId, user.getRole(), false, null);
        user.updatePasswordHash("{noop}bloqueado-" + userId);
        user.resetSecurityState();
        user.bumpTokenVersion();
        appUserPushDeviceRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .forEach(device -> device.revoke(OffsetDateTime.now(ZoneOffset.UTC)));
        appUserRepository.save(user);
    }

    private void anonymizeAgent(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente nao encontrado."));
        agent.anonymizePersonalData();
        agentRepository.save(agent);
    }

    private Map<String, Object> residentSnapshot(Resident resident) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", resident.getId());
        snapshot.put("fullName", resident.getFullName());
        snapshot.put("phoneNumber", resident.getPhoneNumber());
        snapshot.put("address", resident.getAddress());
        snapshot.put("referenceNote", resident.getReferenceNote());
        snapshot.put("status", resident.getStatus().name());
        return snapshot;
    }

    private Map<String, Object> residentAlertSnapshot(ResidentAlert alert) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", alert.getId());
        snapshot.put("type", alert.getType().name());
        snapshot.put("status", alert.getStatus().name());
        snapshot.put("openedAt", alert.getOpenedAt());
        snapshot.put("updatedAt", alert.getUpdatedAt());
        snapshot.put("notes", alert.getNotes());
        snapshot.put("assignedAgentName", alert.getAssignedAgentName());
        snapshot.put("vehiclePlate", alert.getVehiclePlate());
        return snapshot;
    }

    private Map<String, Object> residentSessionSnapshot(ResidentSession session) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", session.getId());
        snapshot.put("createdAt", session.getCreatedAt());
        snapshot.put("expiresAt", session.getExpiresAt());
        snapshot.put("lastSeenAt", session.getLastSeenAt());
        snapshot.put("revokedAt", session.getRevokedAt());
        return snapshot;
    }

    private Map<String, Object> residentPushDeviceSnapshot(ResidentPushDevice device) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", device.getId());
        snapshot.put("deviceLabel", device.getDeviceLabel());
        snapshot.put("createdAt", device.getCreatedAt());
        snapshot.put("updatedAt", device.getUpdatedAt());
        snapshot.put("revokedAt", device.getRevokedAt());
        return snapshot;
    }

    private Map<String, Object> appUserSnapshot(AppUser user) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", user.getId());
        snapshot.put("username", user.getUsername());
        snapshot.put("role", user.getRole().name());
        snapshot.put("enabled", user.isEnabled());
        snapshot.put("createdAt", user.getCreatedAt());
        snapshot.put("tokenVersion", user.getTokenVersion());
        snapshot.put("failedLoginAttempts", user.getFailedLoginAttempts());
        snapshot.put("lockedUntil", user.getLockedUntil());
        return snapshot;
    }

    private Map<String, Object> agentSnapshot(Agent agent) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", agent.getId());
        snapshot.put("fullName", agent.getFullName());
        snapshot.put("badgeCode", agent.getBadgeCode());
        snapshot.put("cnhCategory", agent.getCnhCategory());
        snapshot.put("cnhExpiry", agent.getCnhExpiry());
        snapshot.put("status", agent.getStatus().name());
        snapshot.put("photoUrl", agent.getPhotoUrl());
        snapshot.put("medicalExamExpiry", agent.getMedicalExamExpiry());
        snapshot.put("workExamsExpiry", agent.getWorkExamsExpiry());
        snapshot.put("documentNotes", agent.getDocumentNotes());
        return snapshot;
    }

    private void recordAudit(AuditActionType actionType, String entityName, Long entityId, String description) {
        // Cada pedido ou exportacao vira um evento auditavel para evitar operacao invisivel.
        AuditRecord record = new AuditRecord(
                actionType,
                entityName,
                entityId,
                resolveCurrentActorUsername(),
                OffsetDateTime.now(ZoneOffset.UTC),
                description
        );
        auditRecordRepository.save(record);
    }

    private String resolveCurrentActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return "sistema";
        }

        return authentication.getName();
    }

    private record SubjectSnapshot(Long subjectId, String subjectLabel) {
    }
}
