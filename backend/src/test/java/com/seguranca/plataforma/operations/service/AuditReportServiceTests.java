package com.seguranca.plataforma.operations.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.operations.dto.AuditReportCategory;
import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class AuditReportServiceTests {

    @Test
    void deveConsolidarAuditoriaPorPeriodoSemAutenticacoesPorPadrao() {
        AuditRecordRepository repository = Mockito.mock(AuditRecordRepository.class);
        AuditReportService service = new AuditReportService(repository);

        AuditRecord managementRecord = buildRecord(1L, AuditActionType.UPDATE, "AppUser", "admin", 2, "Atualizacao de usuário");
        AuditRecord fleetRecord = buildRecord(2L, AuditActionType.MAINTENANCE, "VehicleMaintenanceRecord", "oficina", 3, "Manutencao concluida");
        AuditRecord hrRecord = buildRecord(3L, AuditActionType.UPDATE, "Agent", "rh", 4, "Atualizacao de agente");
        AuditRecord operationalRecord = buildRecord(4L, AuditActionType.INCIDENT_WORKFLOW, "Incident", "ronda", 5, "Fluxo de ocorrência");
        AuditRecord securityRecord = buildRecord(5L, AuditActionType.RESIDENT_ALERT, "ResidentAlert", "ronda", 6, "Alerta do morador");
        AuditRecord authRecord = buildRecord(6L, AuditActionType.AUTH, "AuthSession", "admin", 1, "Login bem-sucedido");

        when(repository.findByOccurredAtBetweenOrderByOccurredAtDesc(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(authRecord, securityRecord, operationalRecord, hrRecord, fleetRecord, managementRecord));

        var report = service.buildReport(30, AuditReportCategory.ALL, false);

        assertEquals(5, report.totalRecords());
        assertEquals(5, report.visibleRecords());
        assertFalse(report.records().stream().anyMatch(record -> record.getActionType() == AuditActionType.AUTH));
        assertEquals(1L, report.recordsByCategory().get("GESTAO"));
        assertEquals(1L, report.recordsByCategory().get("FROTA"));
        assertEquals(1L, report.recordsByCategory().get("RH"));
        assertEquals(1L, report.recordsByCategory().get("OPERACIONAL"));
        assertEquals(1L, report.recordsByCategory().get("SEGURANCA"));
        assertEquals("AppUser", report.records().get(0).getEntityName());
    }

    @Test
    void devePermitirRelatorioDeSegurançaComAutenticacoesQuandoSolicitado() {
        AuditRecordRepository repository = Mockito.mock(AuditRecordRepository.class);
        AuditReportService service = new AuditReportService(repository);

        AuditRecord securityRecord = buildRecord(1L, AuditActionType.RESIDENT_ALERT, "ResidentAlert", "ronda", 1, "Alerta do morador");
        AuditRecord authRecord = buildRecord(2L, AuditActionType.AUTH, "AuthSession", "admin", 2, "Login bem-sucedido");

        when(repository.findByOccurredAtBetweenOrderByOccurredAtDesc(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(authRecord, securityRecord));

        var report = service.buildReport(7, AuditReportCategory.SEGURANCA, true);

        assertEquals(2, report.totalRecords());
        assertEquals(2, report.visibleRecords());
        assertEquals(2, report.records().size());
        assertTrue(report.records().stream().anyMatch(record -> record.getActionType() == AuditActionType.AUTH));
        assertEquals(2L, report.recordsByCategory().get("SEGURANCA"));
        assertEquals(1L, report.recordsByActor().get("admin"));
        assertEquals(1L, report.recordsByActor().get("ronda"));
    }

    private AuditRecord buildRecord(Long id, AuditActionType actionType, String entityName, String actorUsername, int daysAgo, String description) {
        AuditRecord record = new AuditRecord(
                actionType,
                entityName,
                10L,
                actorUsername,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(daysAgo),
                description
        );
        ReflectionTestUtils.setField(record, "id", id);
        return record;
    }
}


