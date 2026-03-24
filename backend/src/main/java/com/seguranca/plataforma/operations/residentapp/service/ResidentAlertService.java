package com.seguranca.plataforma.operations.residentapp.service;

import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.model.ResidentStatus;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleStatus;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.operations.repository.VehicleRepository;
import com.seguranca.plataforma.operations.residentapp.dto.CreateResidentAlertRequest;
import com.seguranca.plataforma.operations.residentapp.dto.DispatchResidentAlertRequest;
import com.seguranca.plataforma.operations.residentapp.dto.RegisterResidentPushTokenRequest;
import com.seguranca.plataforma.operations.residentapp.dto.RevokeResidentPushTokenRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentAlertActionNotesRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentAlertCancelRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentAlertResponse;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentLoginRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentProfileResponse;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentSessionResponse;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlert;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertStatus;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertType;
import com.seguranca.plataforma.operations.residentapp.model.ResidentPushDevice;
import com.seguranca.plataforma.operations.residentapp.model.ResidentSession;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentAlertRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentPushDeviceRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentSessionRepository;
import com.seguranca.plataforma.operations.service.OperationsRealtimeService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResidentAlertService {
    // Mantem o fluxo do morador separado da operacao interna para evitar misturar dominios diferentes.

    private static final int SESSION_DAYS = 7;

    private final ResidentRepository residentRepository;
    private final ResidentAlertRepository residentAlertRepository;
    private final ResidentSessionRepository residentSessionRepository;
    private final AgentRepository agentRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditRecordRepository auditRecordRepository;
    private final OperationsRealtimeService operationsRealtimeService;
    private final PasswordEncoder passwordEncoder;
    private final ResidentPushDeviceRepository residentPushDeviceRepository;
    private final ResidentPushNotificationService residentPushNotificationService;

    public ResidentAlertService(
            ResidentRepository residentRepository,
            ResidentAlertRepository residentAlertRepository,
            ResidentSessionRepository residentSessionRepository,
            AgentRepository agentRepository,
            VehicleRepository vehicleRepository,
            AuditRecordRepository auditRecordRepository,
            OperationsRealtimeService operationsRealtimeService,
            PasswordEncoder passwordEncoder,
            ResidentPushDeviceRepository residentPushDeviceRepository,
            ResidentPushNotificationService residentPushNotificationService
    ) {
        this.residentRepository = residentRepository;
        this.residentAlertRepository = residentAlertRepository;
        this.residentSessionRepository = residentSessionRepository;
        this.agentRepository = agentRepository;
        this.vehicleRepository = vehicleRepository;
        this.auditRecordRepository = auditRecordRepository;
        this.operationsRealtimeService = operationsRealtimeService;
        this.passwordEncoder = passwordEncoder;
        this.residentPushDeviceRepository = residentPushDeviceRepository;
        this.residentPushNotificationService = residentPushNotificationService;
    }

    @Transactional
    public ResidentSessionResponse createSession(ResidentLoginRequest request) {
        // Cria uma sessao do morador baseada em PIN dedicado, sem tratar telefone como segredo.
        Resident resident = getResident(request.residentId());
        if (resident.getStatus() != ResidentStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O cadastro do morador esta inativo.");
        }
        if (!resident.isAccessPinConfigured()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O morador ainda nao possui PIN de acesso configurado.");
        }
        if (!passwordEncoder.matches(normalizeResidentPin(request.accessPin()), resident.getAccessPinHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Morador ou PIN invalidos.");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String accessToken = UUID.randomUUID().toString().replace("-", "");
        revokePreviousSessions(resident.getId(), now);
        ResidentSession session = new ResidentSession(
                resident.getId(),
                hashToken(accessToken),
                now,
                now.plusDays(SESSION_DAYS),
                now
        );
        residentSessionRepository.save(session);
        recordAudit(AuditActionType.AUTH, "ResidentSession", resident.getId(), "Sessao do morador " + resident.getFullName());

        return new ResidentSessionResponse(
                "Bearer",
                accessToken,
                session.getExpiresAt(),
                resident.getId(),
                resident.getFullName(),
                resident.getPhoneNumber(),
                resident.getAddress(),
                resident.getReferenceNote()
        );
    }

    @Transactional
    public ResidentProfileResponse getProfile(String authorizationHeader) {
        // Expõe apenas a ficha minima do morador autenticado para a tela inicial do app.
        ResidentSession session = resolveActiveSession(authorizationHeader);
        Resident resident = getResident(session.getResidentId());
        return new ResidentProfileResponse(
                resident.getId(),
                resident.getFullName(),
                resident.getPhoneNumber(),
                resident.getAddress(),
                resident.getReferenceNote(),
                session.getExpiresAt()
        );
    }

    @Transactional
    public List<ResidentAlertResponse> listResidentAlerts(String authorizationHeader) {
        // Entrega o historico proprio do morador sem expor alertas de terceiros.
        ResidentSession session = resolveActiveSession(authorizationHeader);
        return residentAlertRepository.findByResidentIdOrderByOpenedAtDesc(session.getResidentId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ResidentAlertResponse getResidentAlert(String authorizationHeader, Long alertId) {
        ResidentSession session = resolveActiveSession(authorizationHeader);
        return toResponse(getResidentAlert(alertId, session.getResidentId()));
    }

    @Transactional
    public ResidentAlertResponse createAlert(String authorizationHeader, CreateResidentAlertRequest request) {
        // Registra o alerta com a identidade do morador e a localizacao opcional do celular.
        ResidentSession session = resolveActiveSession(authorizationHeader);
        Resident resident = getResident(session.getResidentId());
        if (resident.getStatus() != ResidentStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O cadastro do morador esta inativo.");
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String notes = StringUtils.hasText(request.notes()) ? request.notes().trim() : null;
        boolean silentAlert = request.type() == ResidentAlertType.COACAO;
        String escortDestination = normalizeEscortDestination(request.escortDestination());
        ensureResidentHasNoActiveAlert(resident.getId());

        if (silentAlert) {
            validateCoercionPin(resident, request.coercionPin());
        }
        if (request.type() == ResidentAlertType.ESCOLTA && escortDestination == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o destino da escolta.");
        }

        ResidentAlert alert = new ResidentAlert(
                resident.getId(),
                resident.getFullName(),
                resident.getPhoneNumber(),
                resident.getAddress(),
                request.type(),
                ResidentAlertStatus.OPEN,
                now,
                now,
                request.latitude(),
                request.longitude(),
                notes,
                silentAlert,
                escortDestination
        );

        ResidentAlert savedAlert = residentAlertRepository.save(alert);
        session.touch(now);
        residentSessionRepository.save(session);
        recordAudit(AuditActionType.RESIDENT_ALERT, "ResidentAlert", savedAlert.getId(), "Abertura do alerta " + savedAlert.getType() + " do morador " + resident.getFullName());
        return toResponse(savedAlert);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        // O logout revoga a sessao atual do morador em vez de apenas apagar estado local no celular.
        ResidentSession session = resolveActiveSession(authorizationHeader);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        session.revoke(now);
        residentSessionRepository.save(session);
        recordAudit(AuditActionType.AUTH, "ResidentSession", session.getResidentId(), "Logout do morador");
    }

    @Transactional
    public void registerPushDevice(String authorizationHeader, RegisterResidentPushTokenRequest request) {
        // Vincula o token Expo ao morador autenticado para permitir notificacoes reais do atendimento.
        ResidentSession session = resolveActiveSession(authorizationHeader);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String token = request.expoPushToken().trim();

        residentPushDeviceRepository.findByExpoPushToken(token)
                .ifPresentOrElse(
                        device -> {
                            device.refresh(session.getResidentId(), normalizeDeviceLabel(request.deviceLabel()), now);
                            residentPushDeviceRepository.save(device);
                        },
                        () -> residentPushDeviceRepository.save(
                                new ResidentPushDevice(
                                        session.getResidentId(),
                                        token,
                                        normalizeDeviceLabel(request.deviceLabel()),
                                        now,
                                        now
                                )
                        )
                );
    }

    @Transactional
    public void revokePushDevice(String authorizationHeader, RevokeResidentPushTokenRequest request) {
        // Revoga um token especifico do morador para evitar push em aparelho que saiu de uso.
        ResidentSession session = resolveActiveSession(authorizationHeader);
        residentPushDeviceRepository.findByResidentIdAndExpoPushToken(session.getResidentId(), request.expoPushToken().trim())
                .ifPresent(device -> {
                    device.revoke(OffsetDateTime.now(ZoneOffset.UTC));
                    residentPushDeviceRepository.save(device);
                });
    }

    @Transactional
    public ResidentAlertResponse cancelAlert(String authorizationHeader, Long alertId, ResidentAlertCancelRequest request) {
        // Cancela apenas o proprio alerta enquanto ele ainda esta num estado recuperavel.
        ResidentSession session = resolveActiveSession(authorizationHeader);
        ResidentAlert alert = getResidentAlert(alertId, session.getResidentId());
        if (alert.getStatus() == ResidentAlertStatus.DISPATCHED || alert.getStatus() == ResidentAlertStatus.ON_SITE || alert.getStatus() == ResidentAlertStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e possivel cancelar um alerta ja despachado ou resolvido.");
        }
        if ((alert.getType() == ResidentAlertType.PANICO || alert.getType() == ResidentAlertType.COACAO) && alert.getStatus() == ResidentAlertStatus.ACKNOWLEDGED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alertas criticos recebidos pela central nao podem ser cancelados pelo app.");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        alert.cancel(now, normalizeNotes(request == null ? null : request.cancellationReason(), "Cancelado pelo morador"));
        ResidentAlert savedAlert = residentAlertRepository.save(alert);
        session.touch(now);
        residentSessionRepository.save(session);
        recordAudit(AuditActionType.RESIDENT_ALERT, "ResidentAlert", savedAlert.getId(), "Cancelamento do alerta " + savedAlert.getId());
        residentPushNotificationService.notifyResidentAlertStatusChanged(savedAlert);
        return toResponse(savedAlert);
    }

    @Transactional(readOnly = true)
    public List<ResidentAlertResponse> listAllAlerts() {
        // Visao operacional da central para supervisao e atendimento.
        return residentAlertRepository.findAll().stream()
                .sorted((left, right) -> right.getOpenedAt().compareTo(left.getOpenedAt()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ResidentAlertResponse acknowledge(Long alertId, ResidentAlertActionNotesRequest request) {
        // Marca a recepcao do alerta antes de despachar equipe.
        ResidentAlert alert = getResidentAlert(alertId);
        validateAlertTransition(alert.getStatus(), ResidentAlertStatus.ACKNOWLEDGED);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        alert.acknowledge(now, normalizeNotes(request == null ? null : request.notes(), "Alerta recebido pela central."));
        ResidentAlert savedAlert = residentAlertRepository.save(alert);
        recordAudit(AuditActionType.RESIDENT_ALERT, "ResidentAlert", savedAlert.getId(), "Recebimento do alerta " + savedAlert.getId());
        residentPushNotificationService.notifyResidentAlertStatusChanged(savedAlert);
        return toResponse(savedAlert);
    }

    @Transactional
    public ResidentAlertResponse dispatch(Long alertId, DispatchResidentAlertRequest request) {
        // Relaciona o alerta com vigilante e viatura responsaveis pelo deslocamento.
        ResidentAlert alert = getResidentAlert(alertId);
        validateAlertTransition(alert.getStatus(), ResidentAlertStatus.DISPATCHED);
        Agent agent = getAgent(request.assignedAgentId());
        Vehicle vehicle = getVehicle(request.vehicleId());
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || vehicle.getStatus() == VehicleStatus.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A viatura selecionada nao esta disponivel para despacho.");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        alert.dispatch(
                agent.getId(),
                agent.getFullName(),
                vehicle.getId(),
                vehicle.getPlate(),
                now,
                normalizeNotes(request.dispatchNotes(), null)
        );
        ResidentAlert savedAlert = residentAlertRepository.save(alert);
        recordAudit(AuditActionType.RESIDENT_ALERT, "ResidentAlert", savedAlert.getId(), "Despacho do alerta " + savedAlert.getId());
        residentPushNotificationService.notifyResidentAlertStatusChanged(savedAlert);
        return toResponse(savedAlert);
    }

    @Transactional
    public ResidentAlertResponse markOnSite(Long alertId, ResidentAlertActionNotesRequest request) {
        // Registra chegada sem perder o historico do despacho.
        ResidentAlert alert = getResidentAlert(alertId);
        validateAlertTransition(alert.getStatus(), ResidentAlertStatus.ON_SITE);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        alert.markOnSite(now, normalizeNotes(request == null ? null : request.notes(), "Equipe no local."));
        ResidentAlert savedAlert = residentAlertRepository.save(alert);
        recordAudit(AuditActionType.RESIDENT_ALERT, "ResidentAlert", savedAlert.getId(), "Chegada no local do alerta " + savedAlert.getId());
        residentPushNotificationService.notifyResidentAlertStatusChanged(savedAlert);
        return toResponse(savedAlert);
    }

    @Transactional
    public ResidentAlertResponse resolve(Long alertId, ResidentAlertActionNotesRequest request) {
        // Fecha o ciclo operacional do alerta.
        ResidentAlert alert = getResidentAlert(alertId);
        validateAlertTransition(alert.getStatus(), ResidentAlertStatus.RESOLVED);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        alert.resolve(now, normalizeNotes(request == null ? null : request.notes(), "Alerta resolvido pela operacao."));
        ResidentAlert savedAlert = residentAlertRepository.save(alert);
        recordAudit(AuditActionType.RESIDENT_ALERT, "ResidentAlert", savedAlert.getId(), "Resolucao do alerta " + savedAlert.getId());
        residentPushNotificationService.notifyResidentAlertStatusChanged(savedAlert);
        return toResponse(savedAlert);
    }

    private ResidentSession resolveActiveSession(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao do morador ausente.");
        }

        String token = normalizeBearerToken(authorizationHeader);
        ResidentSession session = residentSessionRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao do morador invalida."));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (!session.isActive(now)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao do morador expirada ou revogada.");
        }

        session.touch(now);
        residentSessionRepository.save(session);
        return session;
    }

    private ResidentAlert getResidentAlert(Long alertId, Long residentId) {
        return residentAlertRepository.findByIdAndResidentId(alertId, residentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta do morador nao encontrado."));
    }

    private ResidentAlert getResidentAlert(Long alertId) {
        return residentAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta do morador nao encontrado."));
    }

    private Resident getResident(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador nao encontrado."));
    }

    private Agent getAgent(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente nao encontrado."));
    }

    private Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viatura nao encontrada."));
    }

    private void validateAlertTransition(ResidentAlertStatus currentStatus, ResidentAlertStatus requestedStatus) {
        // Impede regressao ou saltos inconsistentes no ciclo do alerta.
        if (currentStatus == ResidentAlertStatus.CANCELLED || currentStatus == ResidentAlertStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O alerta ja foi finalizado e nao pode mudar de status.");
        }

        if (currentStatus == ResidentAlertStatus.OPEN && (requestedStatus == ResidentAlertStatus.DISPATCHED || requestedStatus == ResidentAlertStatus.ON_SITE || requestedStatus == ResidentAlertStatus.RESOLVED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O alerta precisa ser recebido pela central antes de seguir para despacho ou resolucao.");
        }
        if (currentStatus == ResidentAlertStatus.ACKNOWLEDGED && requestedStatus == ResidentAlertStatus.ON_SITE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O alerta precisa ser despachado antes de registrar chegada no local.");
        }
        if (currentStatus == ResidentAlertStatus.ACKNOWLEDGED && requestedStatus == ResidentAlertStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O alerta precisa passar por despacho antes da resolucao.");
        }
    }

    private void ensureResidentHasNoActiveAlert(Long residentId) {
        // O morador nao pode abrir varios alertas ativos ao mesmo tempo porque isso quebra o atendimento.
        residentAlertRepository.findFirstByResidentIdAndStatusInOrderByOpenedAtDesc(
                        residentId,
                        List.of(
                                ResidentAlertStatus.OPEN,
                                ResidentAlertStatus.ACKNOWLEDGED,
                                ResidentAlertStatus.DISPATCHED,
                                ResidentAlertStatus.ON_SITE
                        )
                )
                .ifPresent(alert -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ja existe um alerta ativo em atendimento. Aguarde a central concluir ou cancele o alerta atual."
                    );
                });
    }

    private ResidentAlertResponse toResponse(ResidentAlert alert) {
        return new ResidentAlertResponse(
                alert.getId(),
                alert.getResidentId(),
                alert.getResidentName(),
                alert.getResidentPhoneNumber(),
                alert.getResidentAddress(),
                alert.getType(),
                alert.getStatus(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getNotes(),
                alert.isSilent(),
                alert.getEscortDestination(),
                alert.getOpenedAt(),
                alert.getUpdatedAt(),
                alert.getAcknowledgedAt(),
                alert.getDispatchedAt(),
                alert.getOnSiteAt(),
                alert.getResolvedAt(),
                alert.getCancelledAt(),
                alert.getAssignedAgentId(),
                alert.getAssignedAgentName(),
                alert.getVehicleId(),
                alert.getVehiclePlate(),
                alert.getAcknowledgmentNotes(),
                alert.getDispatchNotes(),
                alert.getArrivalNotes(),
                alert.getResolutionNotes(),
                alert.getCancellationReason()
        );
    }

    private String normalizeBearerToken(String authorizationHeader) {
        String trimmed = authorizationHeader.trim();
        if (trimmed.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }

    private String normalizeDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D+", "");
    }

    private String normalizeResidentPin(String pin) {
        String digitsOnly = normalizeDigits(pin);
        if (!StringUtils.hasText(digitsOnly)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o PIN do morador.");
        }
        return digitsOnly;
    }

    private void revokePreviousSessions(Long residentId, OffsetDateTime now) {
        // Nova autenticacao invalida sessoes antigas para reduzir compartilhamento indevido do app.
        residentSessionRepository.findByResidentIdOrderByCreatedAtDesc(residentId).stream()
                .filter(existingSession -> existingSession.isActive(now))
                .forEach(existingSession -> existingSession.revoke(now));
    }

    private void validateCoercionPin(Resident resident, String coercionPin) {
        if (!resident.isCoercionPinConfigured()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O morador nao possui PIN de coacao configurado.");
        }
        if (!passwordEncoder.matches(normalizeResidentPin(coercionPin), resident.getCoercionPinHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "PIN de coacao invalido.");
        }
    }

    private String normalizeEscortDestination(String escortDestination) {
        return StringUtils.hasText(escortDestination) ? escortDestination.trim() : null;
    }

    private String normalizeNotes(String notes, String fallback) {
        return StringUtils.hasText(notes) ? notes.trim() : fallback;
    }

    private String normalizeDeviceLabel(String deviceLabel) {
        return StringUtils.hasText(deviceLabel) ? deviceLabel.trim() : null;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Nao foi possivel gerar o hash do token do morador.", ex);
        }
    }

    private void recordAudit(AuditActionType actionType, String entityName, Long entityId, String description) {
        // Mantem a trilha de auditoria no mesmo padrao da operacao administrativa.
        AuditRecord record = new AuditRecord(
                actionType,
                entityName,
                entityId,
                resolveCurrentActorUsername(),
                OffsetDateTime.now(ZoneOffset.UTC),
                description
        );
        auditRecordRepository.save(record);
        operationsRealtimeService.publish(actionType.name(), entityName, entityId, description);
    }

    private String resolveCurrentActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return "sistema";
        }
        return authentication.getName();
    }
}
