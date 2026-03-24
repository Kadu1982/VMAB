package com.seguranca.plataforma.operations.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.auth.AppUser;
import com.seguranca.plataforma.auth.AppUserRepository;
import com.seguranca.plataforma.auth.AppUserRole;
import com.seguranca.plataforma.auth.AppUserPushNotificationService;
import com.seguranca.plataforma.config.VmabRetentionProperties;
import com.seguranca.plataforma.operations.dto.RondaCloseIncidentRequest;
import com.seguranca.plataforma.operations.dto.RondaDispatchIncidentRequest;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.AgentStatus;
import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.model.IncidentPriority;
import com.seguranca.plataforma.operations.model.IncidentStatus;
import com.seguranca.plataforma.operations.model.IncidentType;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.model.ShiftStatus;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleStatus;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.IncidentEvidenceRepository;
import com.seguranca.plataforma.operations.repository.IncidentRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.operations.repository.ShiftRepository;
import com.seguranca.plataforma.operations.repository.ShiftTelemetryRepository;
import com.seguranca.plataforma.operations.repository.VehicleMaintenanceRecordRepository;
import com.seguranca.plataforma.operations.repository.VehicleRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OperationsServiceIncidentWorkflowTests {

    @Mock private AgentRepository agentRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private AppUserPushNotificationService appUserPushNotificationService;
    @Mock private AuditRecordRepository auditRecordRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleMaintenanceRecordRepository vehicleMaintenanceRecordRepository;
    @Mock private ResidentRepository residentRepository;
    @Mock private ShiftRepository shiftRepository;
    @Mock private ShiftTelemetryRepository shiftTelemetryRepository;
    @Mock private IncidentRepository incidentRepository;
    @Mock private IncidentEvidenceRepository incidentEvidenceRepository;
    @Mock private OperationsRealtimeService operationsRealtimeService;
    @Mock private VehicleFleetReportCalculator vehicleFleetReportCalculator;
    @Mock private PasswordEncoder passwordEncoder;

    private OperationsService operationsService;

    @BeforeEach
    void setUp() {
        VmabRetentionProperties retentionProperties = new VmabRetentionProperties();
        operationsService = new OperationsService(
                agentRepository,
                appUserRepository,
                appUserPushNotificationService,
                auditRecordRepository,
                vehicleRepository,
                vehicleMaintenanceRecordRepository,
                residentRepository,
                shiftRepository,
                shiftTelemetryRepository,
                incidentRepository,
                incidentEvidenceRepository,
                operationsRealtimeService,
                vehicleFleetReportCalculator,
                retentionProperties,
                passwordEncoder,
                "C:/temp/vmab-test-storage"
        );
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("ronda.mobile", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveDespacharOcorrenciaPeloTurnoDaRondaAutenticada() {
        AppUser currentUser = new AppUser("ronda.mobile", "{noop}senha", AppUserRole.RONDA, true, OffsetDateTime.now(ZoneOffset.UTC));
        currentUser.update("ronda.mobile", AppUserRole.RONDA, true, 7L);
        ReflectionTestUtils.setField(currentUser, "linkedAgentId", 7L);
        Incident incident = new Incident(
                IncidentType.PANIC,
                IncidentPriority.HIGH,
                IncidentStatus.OPEN,
                "Morador VMAB",
                "Rua A, 100",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        ReflectionTestUtils.setField(incident, "id", 11L);
        Agent agent = new Agent("Carlos", "CR-007", "B", LocalDate.now().plusYears(1), AgentStatus.ON_DUTY, null, null, null, null);
        ReflectionTestUtils.setField(agent, "id", 7L);
        Vehicle vehicle = new Vehicle(
                "ABC1D23",
                "Trailblazer",
                12000L,
                14000L,
                VehicleStatus.IN_OPERATION,
                LocalDate.now().plusMonths(2),
                LocalDate.now().plusMonths(2),
                LocalDate.now().plusMonths(2),
                LocalDate.now().minusDays(10),
                "Viatura apta"
        );
        ReflectionTestUtils.setField(vehicle, "id", 3L);
        Shift shift = new Shift(
                7L,
                "Carlos",
                3L,
                "ABC1D23",
                ShiftStatus.ACTIVE,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(7),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1),
                12000L,
                80,
                true,
                true,
                true,
                "Checklist ok"
        );

        when(appUserRepository.findByUsername(anyString())).thenReturn(Optional.of(currentUser));
        when(incidentRepository.findById(11L)).thenReturn(Optional.of(incident));
        when(incidentRepository.findAll()).thenReturn(List.of(incident));
        when(shiftRepository.findFirstByAgentIdAndStatusInOrderByStartedAtDesc(
                7L,
                List.of(ShiftStatus.ACTIVE, ShiftStatus.HANDOFF, ShiftStatus.HANDOFF_PENDING)
        )).thenReturn(Optional.of(shift));
        when(agentRepository.findById(7L)).thenReturn(Optional.of(agent));
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicle));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident dispatchedIncident = operationsService.dispatchIncidentForCurrentRonda(11L, new RondaDispatchIncidentRequest("Saindo para atendimento."));

        assertEquals(IncidentStatus.DISPATCHED, dispatchedIncident.getStatus());
        assertEquals(7L, dispatchedIncident.getAssignedAgentId());
        assertEquals("ABC1D23", dispatchedIncident.getVehiclePlate());
        verify(incidentRepository).save(incident);
        verify(appUserPushNotificationService).notifyIncidentWorkflowUpdated(incident);
    }

    @Test
    void naoDeveEncerrarOcorrenciaSemChegadaNoLocal() {
        AppUser currentUser = new AppUser("ronda.mobile", "{noop}senha", AppUserRole.RONDA, true, OffsetDateTime.now(ZoneOffset.UTC));
        currentUser.update("ronda.mobile", AppUserRole.RONDA, true, 7L);
        ReflectionTestUtils.setField(currentUser, "linkedAgentId", 7L);
        Incident incident = new Incident(
                IncidentType.PANIC,
                IncidentPriority.HIGH,
                IncidentStatus.DISPATCHED,
                "Morador VMAB",
                "Rua A, 100",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(10),
                7L,
                "Carlos",
                3L,
                "ABC1D23",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(8),
                null,
                null,
                "Saindo",
                null,
                null
        );
        ReflectionTestUtils.setField(incident, "id", 12L);

        when(appUserRepository.findByUsername(anyString())).thenReturn(Optional.of(currentUser));
        when(incidentRepository.findById(12L)).thenReturn(Optional.of(incident));

        assertThrows(
                ResponseStatusException.class,
                () -> operationsService.closeIncidentForCurrentRonda(12L, new RondaCloseIncidentRequest("Atendimento concluido"))
        );
    }
}
