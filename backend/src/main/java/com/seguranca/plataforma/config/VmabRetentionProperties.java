package com.seguranca.plataforma.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "vmab.retention")
public class VmabRetentionProperties {
    // Centraliza os prazos tecnicos de limpeza para dados temporarios e artefatos sensiveis.

    private boolean enabled = true;

    private String cleanupCron = "0 15 3 * * *";

    @Min(1)
    private int passwordResetTokenRetentionHours = 24;

    @Min(1)
    private int residentSessionRetentionDays = 14;

    @Min(30)
    private int incidentEvidenceRetentionDays = 3650;

    @Min(30)
    private int vehicleMaintenanceRetentionDays = 3650;

    @Min(30)
    private int hrAttendanceRetentionDays = 3650;

    private boolean removeOrphanEvidenceFiles = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCleanupCron() {
        return cleanupCron;
    }

    public void setCleanupCron(String cleanupCron) {
        this.cleanupCron = cleanupCron;
    }

    public int getPasswordResetTokenRetentionHours() {
        return passwordResetTokenRetentionHours;
    }

    public void setPasswordResetTokenRetentionHours(int passwordResetTokenRetentionHours) {
        this.passwordResetTokenRetentionHours = passwordResetTokenRetentionHours;
    }

    public int getResidentSessionRetentionDays() {
        return residentSessionRetentionDays;
    }

    public void setResidentSessionRetentionDays(int residentSessionRetentionDays) {
        this.residentSessionRetentionDays = residentSessionRetentionDays;
    }

    public int getIncidentEvidenceRetentionDays() {
        return incidentEvidenceRetentionDays;
    }

    public void setIncidentEvidenceRetentionDays(int incidentEvidenceRetentionDays) {
        this.incidentEvidenceRetentionDays = incidentEvidenceRetentionDays;
    }

    public int getVehicleMaintenanceRetentionDays() {
        return vehicleMaintenanceRetentionDays;
    }

    public void setVehicleMaintenanceRetentionDays(int vehicleMaintenanceRetentionDays) {
        this.vehicleMaintenanceRetentionDays = vehicleMaintenanceRetentionDays;
    }

    public int getHrAttendanceRetentionDays() {
        return hrAttendanceRetentionDays;
    }

    public void setHrAttendanceRetentionDays(int hrAttendanceRetentionDays) {
        this.hrAttendanceRetentionDays = hrAttendanceRetentionDays;
    }

    public boolean isRemoveOrphanEvidenceFiles() {
        return removeOrphanEvidenceFiles;
    }

    public void setRemoveOrphanEvidenceFiles(boolean removeOrphanEvidenceFiles) {
        this.removeOrphanEvidenceFiles = removeOrphanEvidenceFiles;
    }
}


