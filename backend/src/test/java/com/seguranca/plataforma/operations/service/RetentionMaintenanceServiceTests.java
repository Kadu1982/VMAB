package com.seguranca.plataforma.operations.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.auth.PasswordResetToken;
import com.seguranca.plataforma.auth.PasswordResetTokenRepository;
import com.seguranca.plataforma.config.VmabRetentionProperties;
import com.seguranca.plataforma.hr.model.HrAttendance;
import com.seguranca.plataforma.hr.model.HrAttendanceType;
import com.seguranca.plataforma.hr.repository.HrAttendanceRepository;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.model.IncidentEvidence;
import com.seguranca.plataforma.operations.model.VehicleMaintenancePriority;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceStatus;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceType;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.IncidentEvidenceRepository;
import com.seguranca.plataforma.operations.repository.VehicleMaintenanceRecordRepository;
import com.seguranca.plataforma.operations.residentapp.model.ResidentSession;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentSessionRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RetentionMaintenanceServiceTests {

    @TempDir
    Path tempDir;

    @Test
    void deveRemoverManutencoesERegistrosDePontoExpiradosComAuditoria() throws Exception {
        PasswordResetTokenRepository passwordResetTokenRepository = org.mockito.Mockito.mock(PasswordResetTokenRepository.class);
        ResidentSessionRepository residentSessionRepository = org.mockito.Mockito.mock(ResidentSessionRepository.class);
        IncidentEvidenceRepository incidentEvidenceRepository = org.mockito.Mockito.mock(IncidentEvidenceRepository.class);
        VehicleMaintenanceRecordRepository vehicleMaintenanceRecordRepository = org.mockito.Mockito.mock(VehicleMaintenanceRecordRepository.class);
        HrAttendanceRepository hrAttendanceRepository = org.mockito.Mockito.mock(HrAttendanceRepository.class);
        AuditRecordRepository auditRecordRepository = org.mockito.Mockito.mock(AuditRecordRepository.class);

        VmabRetentionProperties properties = new VmabRetentionProperties();
        properties.setEnabled(true);
        properties.setPasswordResetTokenRetentionHours(24);
        properties.setResidentSessionRetentionDays(14);
        properties.setIncidentEvidenceRetentionDays(3650);
        properties.setVehicleMaintenanceRetentionDays(30);
        properties.setHrAttendanceRetentionDays(30);
        properties.setRemoveOrphanEvidenceFiles(false);

        Path storageRoot = tempDir.resolve("storage");
        Files.createDirectories(storageRoot);

        RetentionMaintenanceService service = new RetentionMaintenanceService(
                properties,
                passwordResetTokenRepository,
                residentSessionRepository,
                incidentEvidenceRepository,
                vehicleMaintenanceRecordRepository,
                hrAttendanceRepository,
                auditRecordRepository,
                storageRoot.toString()
        );

        PasswordResetToken resetToken = new PasswordResetToken(
                1L,
                "admin",
                "token",
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(90),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(80)
        );
        ResidentSession residentSession = new ResidentSession(
                1L,
                "session-token",
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(90),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(80),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(85)
        );
        IncidentEvidence incidentEvidence = new IncidentEvidence(
                9L,
                "foto.jpg",
                "stored-foto.jpg",
                "image/jpeg",
                1024L,
                "Evidencia antiga",
                "ronda",
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(90)
        );
        VehicleMaintenanceRecord maintenanceRecord = new VehicleMaintenanceRecord(
                12L,
                "ABC1D23",
                "OS-RET-01",
                VehicleMaintenanceType.PREVENTIVE,
                VehicleMaintenancePriority.LOW,
                VehicleMaintenanceStatus.COMPLETED,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(90),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(80),
                LocalDate.now(ZoneOffset.UTC).minusDays(80),
                LocalDate.now(ZoneOffset.UTC).plusDays(10),
                1000L,
                2000L,
                new BigDecimal("100.00"),
                "Oficina",
                "Concluida",
                "Descricao",
                true
        );
        HrAttendance attendanceRecord = new HrAttendance(
                5L,
                HrAttendanceType.CHECK_IN,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(90),
                "celular-ronda",
                -23.0,
                -46.0,
                "Ponto antigo",
                false,
                null
        );

        when(passwordResetTokenRepository.findByExpiresAtBefore(any())).thenReturn(List.of(resetToken));
        when(residentSessionRepository.findByExpiresAtBeforeOrRevokedAtBefore(any(), any())).thenReturn(List.of(residentSession));
        when(incidentEvidenceRepository.findByUploadedAtBeforeAndDeletedAtIsNullOrderByUploadedAtAsc(any())).thenReturn(List.of(incidentEvidence));
        when(vehicleMaintenanceRecordRepository.findRetainableRecordsBefore(any())).thenReturn(List.of(maintenanceRecord));
        when(hrAttendanceRepository.findByOccurredAtBeforeOrderByOccurredAtAsc(any())).thenReturn(List.of(attendanceRecord));
        when(auditRecordRepository.save(any(AuditRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cleanupExpiredArtifacts("test");

        verify(passwordResetTokenRepository).deleteAllInBatch(List.of(resetToken));
        verify(residentSessionRepository).deleteAllInBatch(List.of(residentSession));
        verify(incidentEvidenceRepository).deleteAll(List.of(incidentEvidence));
        verify(vehicleMaintenanceRecordRepository).deleteAllInBatch(List.of(maintenanceRecord));
        verify(hrAttendanceRepository).deleteAllInBatch(List.of(attendanceRecord));
        verify(auditRecordRepository).save(any(AuditRecord.class));
    }
}


