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
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.operations.repository.VehicleRepository;
import com.seguranca.plataforma.operations.residentapp.dto.CreateResidentAlertRequest;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlert;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertStatus;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertType;
import com.seguranca.plataforma.operations.residentapp.model.ResidentSession;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentAlertRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentSessionRepository;
import com.seguranca.plataforma.operations.service.OperationsRealtimeService;
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
    void naoDeveAbrirNovoAlertaQuandoJaExisteUmAtivo() {
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
