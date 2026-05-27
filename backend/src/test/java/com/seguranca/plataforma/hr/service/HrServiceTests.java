package com.seguranca.plataforma.hr.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.auth.AppUserRepository;
import com.seguranca.plataforma.hr.dto.HrAttendanceResponse;
import com.seguranca.plataforma.hr.dto.HrEmployeeResponse;
import com.seguranca.plataforma.hr.dto.HrSummaryResponse;
import com.seguranca.plataforma.hr.dto.RecordHrAttendanceRequest;
import com.seguranca.plataforma.hr.model.HrAttendance;
import com.seguranca.plataforma.hr.model.HrAttendanceType;
import com.seguranca.plataforma.hr.model.HrEmployee;
import com.seguranca.plataforma.hr.model.HrEmployeeCategory;
import com.seguranca.plataforma.hr.model.HrEmployeeStatus;
import com.seguranca.plataforma.hr.repository.HrAttendanceRepository;
import com.seguranca.plataforma.hr.repository.HrEmployeeRepository;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.AgentStatus;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HrServiceTests {

    @Mock private HrEmployeeRepository hrEmployeeRepository;
    @Mock private HrAttendanceRepository hrAttendanceRepository;
    @Mock private AgentRepository agentRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private AuditRecordRepository auditRecordRepository;

    private HrService hrService;

    @BeforeEach
    void setUp() {
        hrService = new HrService(
                hrEmployeeRepository,
                hrAttendanceRepository,
                agentRepository,
                appUserRepository,
                auditRecordRepository
        );
    }

    @Test
    void deveSincronizarAgenteComoFuncionarioRH() {
        Agent agent = new Agent(
                "Carlos Nunes",
                "ALPHA-01",
                "AB",
                LocalDate.now().plusDays(40),
                null,
                AgentStatus.ACTIVE,
                "https://example.com/foto.png",
                "Observacao inicial"
        );

        when(hrEmployeeRepository.findByLinkedAgentId(7L)).thenReturn(Optional.empty());
        when(hrEmployeeRepository.save(any(HrEmployee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HrEmployeeResponse response = hrService.syncAgent(7L, agent);

        ArgumentCaptor<HrEmployee> employeeCaptor = ArgumentCaptor.forClass(HrEmployee.class);
        verify(hrEmployeeRepository).save(employeeCaptor.capture());

        HrEmployee savedEmployee = employeeCaptor.getValue();
        assertEquals("ALPHA-01", savedEmployee.getEmployeeCode());
        assertEquals("Carlos Nunes", savedEmployee.getFullName());
        assertEquals(HrEmployeeStatus.ACTIVE, savedEmployee.getStatus());
        assertEquals(HrEmployeeCategory.VIGILANTE, savedEmployee.getCategory());
        assertEquals(7L, savedEmployee.getLinkedAgentId());
        assertEquals("AB", savedEmployee.getCnhCategory());
        assertEquals(agent.getCnhExpiry(), savedEmployee.getCnhExpiry());
        assertEquals("Carlos Nunes", response.fullName());
        assertEquals("ALPHA-01", response.employeeCode());
    }

    @Test
    void deveRegistrarCheckinEAtualizarUltimoPontoDoFuncionario() {
        HrEmployee employee = new HrEmployee(
                "EMP-001",
                "Marina Luz",
                HrEmployeeCategory.VIGILANTE,
                HrEmployeeStatus.ACTIVE,
                null,
                null,
                null,
                null,
                "AB",
                LocalDate.now().plusDays(60),
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(45),
                "Treinamento em andamento",
                "Documento ok",
                LocalDate.now().minusMonths(4),
                null,
                null,
                null,
                true
        );
        HrEmployee savedEmployee = employee;

        when(hrEmployeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(hrAttendanceRepository.save(any(HrAttendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hrEmployeeRepository.save(any(HrEmployee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HrAttendanceResponse response = hrService.recordAttendance(
                1L,
                new RecordHrAttendanceRequest(
                        HrAttendanceType.CHECK_IN,
                        OffsetDateTime.now(ZoneOffset.UTC),
                        "celular-ronda",
                        -23.55052,
                        -46.63331,
                        "Início do turno",
                        false,
                        null
                )
        );

        verify(hrAttendanceRepository).save(any(HrAttendance.class));
        verify(hrEmployeeRepository).save(any(HrEmployee.class));
        assertEquals(HrAttendanceType.CHECK_IN, response.eventType());
        assertTrue(savedEmployee.getLastCheckInAt() != null);
        assertEquals("celular-ronda", savedEmployee.getLastCheckInDevice());
        assertEquals(-23.55052, savedEmployee.getLastCheckInLatitude());
        assertEquals(-46.63331, savedEmployee.getLastCheckInLongitude());
    }

    @Test
    void deveGerarAlertaDeRenovacaoQuandoCNhEstiverPertoDoVencimento() {
        HrEmployee employee = new HrEmployee(
                "EMP-002",
                "Joao Prado",
                HrEmployeeCategory.VIGILANTE,
                HrEmployeeStatus.ACTIVE,
                null,
                null,
                null,
                null,
                "B",
                LocalDate.now().plusDays(12),
                LocalDate.now().plusDays(90),
                LocalDate.now().plusDays(90),
                null,
                null,
                LocalDate.now().minusMonths(2),
                null,
                null,
                null,
                true
        );
        when(hrEmployeeRepository.findAllByOrderByIdAsc()).thenReturn(List.of(employee));
        when(hrAttendanceRepository.findTop20ByOrderByOccurredAtDesc()).thenReturn(List.of());

        HrSummaryResponse summary = hrService.summary();

        assertEquals(1, summary.expiringSoonAlerts());
        assertEquals(1, summary.employees().size());
        assertEquals(1, summary.alerts().size());
        assertEquals("CNH", summary.alerts().getFirst().alertType());
        assertTrue(summary.alerts().getFirst().daysRemaining() <= 30);
        assertTrue(summary.attendance().isEmpty());
    }
}
