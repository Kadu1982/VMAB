package com.seguranca.plataforma.privacy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguranca.plataforma.auth.AppUser;
import com.seguranca.plataforma.auth.AppUserPushDevice;
import com.seguranca.plataforma.auth.AppUserPushDeviceRepository;
import com.seguranca.plataforma.auth.AppUserRepository;
import com.seguranca.plataforma.auth.AppUserRole;
import com.seguranca.plataforma.auth.PasswordResetTokenRepository;
import com.seguranca.plataforma.config.VmabRetentionProperties;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.AgentStatus;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.model.ResidentStatus;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.operations.repository.ShiftRepository;
import com.seguranca.plataforma.operations.residentapp.model.ResidentPushDevice;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentAlertRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentPushDeviceRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentSessionRepository;
import com.seguranca.plataforma.privacy.dto.PrivacyExportResponse;
import com.seguranca.plataforma.privacy.dto.PrivacyRequestResponse;
import com.seguranca.plataforma.privacy.dto.UpdatePrivacyRequestRequest;
import com.seguranca.plataforma.privacy.model.PrivacyRequest;
import com.seguranca.plataforma.privacy.model.PrivacyRequestStatus;
import com.seguranca.plataforma.privacy.model.PrivacyRequestType;
import com.seguranca.plataforma.privacy.model.PrivacySubjectType;
import com.seguranca.plataforma.privacy.repository.PrivacyRequestRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PrivacyServiceTests {

    @Mock private PrivacyRequestRepository privacyRequestRepository;
    @Mock private ResidentRepository residentRepository;
    @Mock private ResidentAlertRepository residentAlertRepository;
    @Mock private ResidentSessionRepository residentSessionRepository;
    @Mock private ResidentPushDeviceRepository residentPushDeviceRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private AppUserPushDeviceRepository appUserPushDeviceRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private AgentRepository agentRepository;
    @Mock private ShiftRepository shiftRepository;
    @Mock private AuditRecordRepository auditRecordRepository;

    private PrivacyService privacyService;

    @BeforeEach
    void setUp() {
        privacyService = new PrivacyService(
                privacyRequestRepository,
                residentRepository,
                residentAlertRepository,
                residentSessionRepository,
                new VmabRetentionProperties(),
                appUserRepository,
                appUserPushDeviceRepository,
                passwordResetTokenRepository,
                agentRepository,
                shiftRepository,
                residentPushDeviceRepository,
                auditRecordRepository,
                new ObjectMapper()
        );
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveIncluirDispositivosPushNaExportacaoDoMorador() {
        Resident resident = new Resident("Ana Souza", "11999990000", "Rua A", "Casa azul", ResidentStatus.ACTIVE, "hash", "hash2");
        ReflectionTestUtils.setField(resident, "id", 3L);
        ResidentPushDevice device = new ResidentPushDevice(3L, "ExponentPushToken[resident]", "Moto G", OffsetDateTime.now(ZoneOffset.UTC).minusDays(2), OffsetDateTime.now(ZoneOffset.UTC));
        ReflectionTestUtils.setField(device, "id", 9L);

        when(residentRepository.findById(3L)).thenReturn(Optional.of(resident));
        when(residentAlertRepository.findByResidentIdOrderByOpenedAtDesc(3L)).thenReturn(List.of());
        when(residentSessionRepository.findByResidentIdOrderByCreatedAtDesc(3L)).thenReturn(List.of());
        when(residentPushDeviceRepository.findByResidentIdOrderByUpdatedAtDesc(3L)).thenReturn(List.of(device));
        when(privacyRequestRepository.findAllByOrderByRequestedAtDesc()).thenReturn(List.of());

        PrivacyExportResponse response = privacyService.exportSubject(PrivacySubjectType.RESIDENT, 3L);

        Map<?, ?> payload = response.payload();
        List<?> pushDevices = (List<?>) payload.get("pushDevices");
        assertNotNull(pushDevices);
        assertEquals(1, pushDevices.size());
    }

    @Test
    void deveRevogarDispositivosPushAoConcluirExclusaoDoUsuario() {
        AppUser user = new AppUser("ronda.mobile", "hash", AppUserRole.RONDA, true, OffsetDateTime.now(ZoneOffset.UTC).minusDays(5));
        ReflectionTestUtils.setField(user, "id", 7L);
        AppUserPushDevice device = new AppUserPushDevice(7L, "ExponentPushToken[user]", "Samsung", OffsetDateTime.now(ZoneOffset.UTC).minusDays(1), OffsetDateTime.now(ZoneOffset.UTC));
        ReflectionTestUtils.setField(device, "id", 14L);
        PrivacyRequest request = new PrivacyRequest(
                PrivacyRequestType.DELETE,
                PrivacySubjectType.APP_USER,
                7L,
                "ronda.mobile",
                PrivacyRequestStatus.OPEN,
                "admin",
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(2),
                "Excluir usuario"
        );
        ReflectionTestUtils.setField(request, "id", 30L);

        when(privacyRequestRepository.findById(30L)).thenReturn(Optional.of(request));
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(user));
        when(appUserPushDeviceRepository.findByUserIdOrderByUpdatedAtDesc(7L)).thenReturn(List.of(device));
        when(privacyRequestRepository.save(any(PrivacyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrivacyRequestResponse response = privacyService.updateRequest(
                30L,
                new UpdatePrivacyRequestRequest(PrivacyRequestStatus.COMPLETED, "Atendido", true, "email", "Titular comunicado por email")
        );

        assertEquals(PrivacyRequestStatus.COMPLETED, response.status());
        assertNotNull(device.getRevokedAt());
        verify(appUserRepository).save(user);
    }

    @Test
    void deveGerarRascunhoDeNotificacaoDoTitular() {
        PrivacyRequest request = new PrivacyRequest(
                PrivacyRequestType.EXPORT,
                PrivacySubjectType.AGENT,
                5L,
                "Carlos",
                PrivacyRequestStatus.IN_PROGRESS,
                "admin",
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1),
                "Atendimento em curso"
        );
        request.updateStatus(PrivacyRequestStatus.IN_PROGRESS, "admin", OffsetDateTime.now(ZoneOffset.UTC), "Em separacao");
        request.registerSubjectNotification("email", "Rascunho preparado", OffsetDateTime.now(ZoneOffset.UTC));
        ReflectionTestUtils.setField(request, "id", 31L);

        when(privacyRequestRepository.findById(31L)).thenReturn(Optional.of(request));
        String draft = privacyService.buildNotificationDraft(31L);

        assertNotNull(draft);
        assertTrue(draft.contains("Carlos"));
        assertTrue(draft.contains("email"));
    }
}
