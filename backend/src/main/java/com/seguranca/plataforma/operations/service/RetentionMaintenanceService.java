package com.seguranca.plataforma.operations.service;

import com.seguranca.plataforma.auth.PasswordResetTokenRepository;
import com.seguranca.plataforma.config.VmabRetentionProperties;
import com.seguranca.plataforma.hr.model.HrAttendance;
import com.seguranca.plataforma.hr.repository.HrAttendanceRepository;
import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.model.IncidentEvidence;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.IncidentEvidenceRepository;
import com.seguranca.plataforma.operations.repository.VehicleMaintenanceRecordRepository;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentSessionRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetentionMaintenanceService {
    // Remove artefatos temporarios e evidência expirada sem depender de operação manual.

    private final VmabRetentionProperties retentionProperties;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ResidentSessionRepository residentSessionRepository;
    private final IncidentEvidenceRepository incidentEvidenceRepository;
    private final VehicleMaintenanceRecordRepository vehicleMaintenanceRecordRepository;
    private final HrAttendanceRepository hrAttendanceRepository;
    private final AuditRecordRepository auditRecordRepository;
    private final Path storageRoot;

    public RetentionMaintenanceService(
            VmabRetentionProperties retentionProperties,
            PasswordResetTokenRepository passwordResetTokenRepository,
            ResidentSessionRepository residentSessionRepository,
            IncidentEvidenceRepository incidentEvidenceRepository,
            VehicleMaintenanceRecordRepository vehicleMaintenanceRecordRepository,
            HrAttendanceRepository hrAttendanceRepository,
            AuditRecordRepository auditRecordRepository,
            @Value("${vmab.storage-root}") String storageRoot
    ) {
        this.retentionProperties = retentionProperties;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.residentSessionRepository = residentSessionRepository;
        this.incidentEvidenceRepository = incidentEvidenceRepository;
        this.vehicleMaintenanceRecordRepository = vehicleMaintenanceRecordRepository;
        this.hrAttendanceRepository = hrAttendanceRepository;
        this.auditRecordRepository = auditRecordRepository;
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initializeStorageRoot() {
        // Garante o diretorio base antes da primeira limpeza agendada.
        try {
            Files.createDirectories(storageRoot.resolve("incidents"));
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível preparar o diretório de retenção.", exception);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cleanOnStartup() {
        // Executa uma passada inicial para remover lixo antigo assim que a aplicacao sobe.
        cleanupExpiredArtifacts("startup");
    }

    @Scheduled(cron = "${vmab.retention.cleanup-cron}")
    @Transactional
    public void cleanOnSchedule() {
        // Mantem a base enxuta com uma varredura periodica de retenção.
        cleanupExpiredArtifacts("schedule");
    }

    public void cleanupExpiredArtifacts(String trigger) {
        if (!retentionProperties.isEnabled()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime passwordResetCutoff = now.minusHours(retentionProperties.getPasswordResetTokenRetentionHours());
        OffsetDateTime residentSessionCutoff = now.minusDays(retentionProperties.getResidentSessionRetentionDays());
        OffsetDateTime evidenceCutoff = now.minusDays(retentionProperties.getIncidentEvidenceRetentionDays());
        OffsetDateTime maintenanceCutoff = now.minusDays(retentionProperties.getVehicleMaintenanceRetentionDays());
        OffsetDateTime attendanceCutoff = now.minusDays(retentionProperties.getHrAttendanceRetentionDays());

        List<com.seguranca.plataforma.auth.PasswordResetToken> expiredPasswordResetTokens = passwordResetTokenRepository.findByExpiresAtBefore(passwordResetCutoff);
        long removedPasswordResetTokens = expiredPasswordResetTokens.size();
        passwordResetTokenRepository.deleteAllInBatch(expiredPasswordResetTokens);

        List<com.seguranca.plataforma.operations.residentapp.model.ResidentSession> expiredResidentSessions =
                residentSessionRepository.findByExpiresAtBeforeOrRevokedAtBefore(residentSessionCutoff, residentSessionCutoff);
        long removedResidentSessions = expiredResidentSessions.size();
        residentSessionRepository.deleteAllInBatch(expiredResidentSessions);
        List<IncidentEvidence> expiredEvidence = incidentEvidenceRepository.findByUploadedAtBeforeAndDeletedAtIsNullOrderByUploadedAtAsc(evidenceCutoff);
        incidentEvidenceRepository.deleteAll(expiredEvidence);

        List<VehicleMaintenanceRecord> expiredMaintenanceRecords = vehicleMaintenanceRecordRepository.findRetainableRecordsBefore(maintenanceCutoff);
        long removedMaintenanceRecords = expiredMaintenanceRecords.size();
        vehicleMaintenanceRecordRepository.deleteAllInBatch(expiredMaintenanceRecords);

        List<HrAttendance> expiredAttendanceRecords = hrAttendanceRepository.findByOccurredAtBeforeOrderByOccurredAtAsc(attendanceCutoff);
        long removedAttendanceRecords = expiredAttendanceRecords.size();
        hrAttendanceRepository.deleteAllInBatch(expiredAttendanceRecords);

        int removedEvidenceFiles = deleteEvidenceFiles(expiredEvidence);
        int removedOrphanEvidenceFiles = retentionProperties.isRemoveOrphanEvidenceFiles() ? deleteOrphanEvidenceFiles() : 0;

        if (removedPasswordResetTokens > 0
                || removedResidentSessions > 0
                || !expiredEvidence.isEmpty()
                || removedMaintenanceRecords > 0
                || removedAttendanceRecords > 0
                || removedEvidenceFiles > 0
                || removedOrphanEvidenceFiles > 0) {
            recordAudit(
                    "Limpeza de retenção executada via " + trigger
                            + ": resetTokens=" + removedPasswordResetTokens
                            + ", residentSessions=" + removedResidentSessions
                            + ", evidências=" + expiredEvidence.size()
                            + ", manutencoes=" + removedMaintenanceRecords
                            + ", pontos=" + removedAttendanceRecords
                            + ", arquivosRemovidos=" + removedEvidenceFiles
                            + ", arquivosOrfaos=" + removedOrphanEvidenceFiles
            );
        }
    }

    private int deleteEvidenceFiles(List<IncidentEvidence> evidences) {
        int deletedFiles = 0;
        for (IncidentEvidence evidence : evidences) {
            Path evidencePath = storageRoot
                    .resolve("incidents")
                    .resolve(String.valueOf(evidence.getIncidentId()))
                    .resolve(evidence.getStoredFilename());
            deletedFiles += deleteIfExists(evidencePath) ? 1 : 0;
            pruneEmptyParentDirectories(evidencePath.getParent());
        }
        return deletedFiles;
    }

    private int deleteOrphanEvidenceFiles() {
        Path incidentsRoot = storageRoot.resolve("incidents");
        if (!Files.exists(incidentsRoot)) {
            return 0;
        }

        Set<String> knownRelativePaths = new HashSet<>();
        for (IncidentEvidence evidence : incidentEvidenceRepository.findAll()) {
            knownRelativePaths.add(String.valueOf(evidence.getIncidentId()) + "/" + evidence.getStoredFilename());
        }

        int deletedFiles = 0;
        try (var paths = Files.walk(incidentsRoot)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.naturalOrder())
                    .toList();

            for (Path file : files) {
                String relativePath = incidentsRoot.relativize(file).toString().replace('\\', '/');
                if (!knownRelativePaths.contains(relativePath) && deleteIfExists(file)) {
                    deletedFiles++;
                    pruneEmptyParentDirectories(file.getParent());
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possivel varrer os arquivos de evidência para retenção.", exception);
        }

        return deletedFiles;
    }

    private boolean deleteIfExists(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException exception) {
            return false;
        }
    }

    private void pruneEmptyParentDirectories(Path directory) {
        Path incidentsRoot = storageRoot.resolve("incidents");
        Path current = directory;
        while (current != null && current.startsWith(incidentsRoot) && !current.equals(incidentsRoot)) {
            try (var listing = Files.list(current)) {
                if (listing.findAny().isPresent()) {
                    return;
                }
            } catch (IOException exception) {
                return;
            }

            try {
                Files.deleteIfExists(current);
            } catch (IOException exception) {
                return;
            }

            current = current.getParent();
        }
    }

    private void recordAudit(String description) {
        // Registra a limpeza para que a retenção tambem fique auditavel.
        AuditRecord record = new AuditRecord(
                AuditActionType.DELETE,
                "RetentionCleanup",
                null,
                "sistema",
                OffsetDateTime.now(ZoneOffset.UTC),
                description
        );
        auditRecordRepository.save(record);
    }
}

