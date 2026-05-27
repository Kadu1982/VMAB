package com.seguranca.plataforma.operations.residentapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.model.ResidentStatus;
import com.seguranca.plataforma.operations.dto.ActivePatrolResponse;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.operations.repository.VehicleRepository;
import com.seguranca.plataforma.operations.residentapp.dto.CreateResidentAlertRequest;
import com.seguranca.plataforma.operations.residentapp.dto.RegisterResidentPushTokenRequest;
import com.seguranca.plataforma.operations.residentapp.dto.RevokeResidentPushTokenRequest;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlert;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertStatus;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertType;
import com.seguranca.plataforma.operations.residentapp.model.ResidentPushDevice;
import com.seguranca.plataforma.operations.residentapp.model.ResidentSession;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentAlertRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentPushDeviceRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentSessionRepository;
import com.seguranca.plataforma.operations.service.OperationsRealtimeService;
import com.seguranca.plataforma.operations.service.OperationsService;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ResidentAlertServiceTests {

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private ResidentAlertRepository residentAlertRepository;

    @Mock
    private ResidentSessionRepository residentSessionRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditRecordRepository auditRecordRepository;

    @Mock
    private OperationsRealtimeService operationsRealtimeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ResidentPushDeviceRepository residentPushDeviceRepository;

    @Mock
    private ResidentPushNotificationService residentPushNotificationService;

    @Mock
    private OperationsService operationsService;

    @InjectMocks
    private ResidentAlertService residentAlertService;

    private Resident resident;
    private ResidentSession residentSession;

    @BeforeEach
    void setUp() {
        resident = new Resident(
                "Morador VMAB",
                "(11) 99999-1111",
                "Rua A, 100",
                null,
                ResidentStatus.ACTIVE,
                "{bcrypt}pin",
                "{bcrypt}coercao"
        );
        ReflectionTestUtils.setField(resident, "id", 1L);
        residentSession = new ResidentSession(
                1L,
                hashToken("token-de-teste"),
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    @Test
    void deveRevogarSessaoNoLogoutDoMorador() {
        when(residentSessionRepository.findByTokenHash(anyString())).thenReturn(Optional.of(residentSession));

        assertDoesNotThrow(() -> residentAlertService.logout("Bearer token-de-teste"));

        verify(residentSessionRepository, atLeast(2)).save(residentSession);
        assertNotNull(residentSession.getRevokedAt());
    }

    @Test
    void deveRegistrarTokenExpoDoMorador() {
        when(residentSessionRepository.findByTokenHash(anyString())).thenReturn(Optional.of(residentSession));
        when(residentPushDeviceRepository.findByExpoPushToken("ExponentPushToken[abc]")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> residentAlertService.registerPushDevice(
                "Bearer token-de-teste",
                new RegisterResidentPushTokenRequest("ExponentPushToken[abc]", "android")
        ));

        verify(residentPushDeviceRepository).save(any(ResidentPushDevice.class));
    }

    @Test
    void deveRevogarTokenExpoDoMoradorNoLogoutDoDispositivo() {
        ResidentPushDevice pushDevice = new ResidentPushDevice(
                1L,
                "ExponentPushToken[abc]",
                "android",
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(10)
        );
        when(residentSessionRepository.findByTokenHash(anyString())).thenReturn(Optional.of(residentSession));
        when(residentPushDeviceRepository.findByResidentIdAndExpoPushToken(1L, "ExponentPushToken[abc]")).thenReturn(Optional.of(pushDevice));

        assertDoesNotThrow(() -> residentAlertService.revokePushDevice(
                "Bearer token-de-teste",
                new RevokeResidentPushTokenRequest("ExponentPushToken[abc]")
        ));

        verify(residentPushDeviceRepository).save(pushDevice);
        assertNotNull(pushDevice.getRevokedAt());
    }

    @Test
    void nãoDeveAbrirNovoAlertaQuandoJaExisteUmAtivo() {
        when(residentSessionRepository.findByTokenHash(anyString())).thenReturn(Optional.of(residentSession));
        when(residentRepository.findById(1L)).thenReturn(Optional.of(resident));
        when(residentAlertRepository.findFirstByResidentIdAndStatusInOrderByOpenedAtDesc(
                1L,
                List.of(
                        ResidentAlertStatus.OPEN,
                        ResidentAlertStatus.ACKNOWLEDGED,
                        ResidentAlertStatus.DISPATCHED,
                        ResidentAlertStatus.ON_SITE
                )
        )).thenReturn(Optional.of(activeAlert()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> residentAlertService.createAlert(
                        "Bearer token-de-teste",
                        new CreateResidentAlertRequest(ResidentAlertType.PANICO, "Teste", null, null, null, null)
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(residentAlertRepository, never()).save(any(ResidentAlert.class));
    }

    @Test
    void deveExigirDestinoNaEscolta() {
        when(residentSessionRepository.findByTokenHash(anyString())).thenReturn(Optional.of(residentSession));
        when(residentRepository.findById(1L)).thenReturn(Optional.of(resident));
        when(residentAlertRepository.findFirstByResidentIdAndStatusInOrderByOpenedAtDesc(
                1L,
                List.of(
                        ResidentAlertStatus.OPEN,
                        ResidentAlertStatus.ACKNOWLEDGED,
                        ResidentAlertStatus.DISPATCHED,
                        ResidentAlertStatus.ON_SITE
                )
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> residentAlertService.createAlert(
                        "Bearer token-de-teste",
                        new CreateResidentAlertRequest(ResidentAlertType.ESCOLTA, "Teste", null, null, null, null)
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deveExigirPinDeCoacaoValido() {
        when(residentSessionRepository.findByTokenHash(anyString())).thenReturn(Optional.of(residentSession));
        when(residentRepository.findById(1L)).thenReturn(Optional.of(resident));
        when(residentAlertRepository.findFirstByResidentIdAndStatusInOrderByOpenedAtDesc(
                1L,
                List.of(
                        ResidentAlertStatus.OPEN,
                        ResidentAlertStatus.ACKNOWLEDGED,
                        ResidentAlertStatus.DISPATCHED,
                        ResidentAlertStatus.ON_SITE
                )
        )).thenReturn(Optional.empty());
        when(passwordEncoder.matches("4455", resident.getCoercionPinHash())).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> residentAlertService.createAlert(
                        "Bearer token-de-teste",
                        new CreateResidentAlertRequest(ResidentAlertType.COACAO, "Teste", null, "4455", null, null)
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void deveEnviarPushAoReconhecerAlerta() {
        ResidentAlert alert = activeAlert();
        ReflectionTestUtils.setField(alert, "id", 10L);
        when(residentAlertRepository.findById(10L)).thenReturn(Optional.of(alert));
        when(residentAlertRepository.save(any(ResidentAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        residentAlertService.acknowledge(10L, null);

        verify(residentPushNotificationService).notifyResidentAlertStatusChanged(any(ResidentAlert.class));
    }

    @Test
    void deveEntregarPatrulhaVisivelAoMoradorAutenticado() {
        ActivePatrolResponse activePatrol = new ActivePatrolResponse(
                1L,
                1L,
                "Carlos Nunes",
                "ALPHA-01",
                null,
                "ABC1D23",
                "Duster",
                48241L,
                "IN_OPERATION",
                -23.56,
                -46.65,
                18.0,
                22.0,
                4.2,
                100,
                OffsetDateTime.now(ZoneOffset.UTC),
                List.of(),
                List.of()
        );
        when(residentSessionRepository.findByTokenHash(anyString())).thenReturn(Optional.of(residentSession));
        when(operationsService.activePatrolSummary()).thenReturn(activePatrol);

        ActivePatrolResponse response = residentAlertService.getVisiblePatrol("Bearer token-de-teste");

        assertEquals(activePatrol, response);
    }

    private ResidentAlert activeAlert() {
        return new ResidentAlert(
                1L,
                resident.getFullName(),
                resident.getPhoneNumber(),
                resident.getAddress(),
                ResidentAlertType.PANICO,
                ResidentAlertStatus.OPEN,
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(10),
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(10),
                null,
                null,
                "Teste",
                false,
                null
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes()));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}


