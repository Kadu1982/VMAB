package com.seguranca.plataforma.operations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "agent_name", nullable = false)
    private String agentName;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "vehicle_plate", nullable = false)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "scheduled_start_at", nullable = false)
    private OffsetDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private OffsetDateTime scheduledEndAt;

    @Column(name = "check_in_at")
    private OffsetDateTime checkInAt;

    @Column(name = "check_out_at")
    private OffsetDateTime checkOutAt;

    @Column(name = "start_km")
    private Long startKm;

    @Column(name = "end_km")
    private Long endKm;

    @Column(name = "handoff_from_agent_id")
    private Long handoffFromAgentId;

    @Column(name = "handoff_from_agent_name")
    private String handoffFromAgentName;

    @Column(name = "handoff_to_agent_id")
    private Long handoffToAgentId;

    @Column(name = "handoff_to_agent_name")
    private String handoffToAgentName;

    @Column(name = "handoff_accepted_at")
    private OffsetDateTime handoffAcceptedAt;

    @Column(name = "handoff_requested_at")
    private OffsetDateTime handoffRequestedAt;

    @Column(name = "handoff_requested_by")
    private String handoffRequestedBy;

    @Column(name = "handoff_rejected_at")
    private OffsetDateTime handoffRejectedAt;

    @Column(name = "handoff_rejected_by")
    private String handoffRejectedBy;

    @Column(name = "handoff_rejection_reason", length = 500)
    private String handoffRejectionReason;

    @Column(name = "handoff_notes", length = 500)
    private String handoffNotes;

    @Column(name = "fuel_level_percent")
    private Integer fuelLevelPercent;

    @Column(name = "tires_checked", nullable = false)
    private boolean tiresChecked;

    @Column(name = "lights_checked", nullable = false)
    private boolean lightsChecked;

    @Column(name = "documents_checked", nullable = false)
    private boolean documentsChecked;

    @Column(name = "checklist_notes", length = 500)
    private String checklistNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private ShiftAttendanceStatus attendanceStatus;

    @Column(name = "late_minutes")
    private Integer lateMinutes;

    @Column(name = "coverage_for_agent_id")
    private Long coverageForAgentId;

    @Column(name = "coverage_for_agent_name")
    private String coverageForAgentName;

    @Column(name = "attendance_notes", length = 500)
    private String attendanceNotes;

    protected Shift() {
    }

    public Shift(
            Long agentId,
            String agentName,
            Long vehicleId,
            String vehiclePlate,
            ShiftStatus status,
            OffsetDateTime startedAt,
            OffsetDateTime scheduledStartAt,
            OffsetDateTime scheduledEndAt,
            OffsetDateTime checkInAt,
            Long startKm,
            Integer fuelLevelPercent,
            boolean tiresChecked,
            boolean lightsChecked,
            boolean documentsChecked,
            String checklistNotes
    ) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.status = status;
        this.startedAt = startedAt;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.checkInAt = checkInAt;
        this.startKm = startKm;
        this.fuelLevelPercent = fuelLevelPercent;
        this.tiresChecked = tiresChecked;
        this.lightsChecked = lightsChecked;
        this.documentsChecked = documentsChecked;
        this.checklistNotes = checklistNotes;
        this.attendanceStatus = ShiftAttendanceStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getScheduledEndAt() {
        return scheduledEndAt;
    }

    public OffsetDateTime getScheduledStartAt() {
        return scheduledStartAt;
    }

    public OffsetDateTime getCheckInAt() {
        return checkInAt;
    }

    public OffsetDateTime getCheckOutAt() {
        return checkOutAt;
    }

    public Long getStartKm() {
        return startKm;
    }

    public Long getEndKm() {
        return endKm;
    }

    public Long getHandoffFromAgentId() {
        return handoffFromAgentId;
    }

    public String getHandoffFromAgentName() {
        return handoffFromAgentName;
    }

    public Long getHandoffToAgentId() {
        return handoffToAgentId;
    }

    public String getHandoffToAgentName() {
        return handoffToAgentName;
    }

    public OffsetDateTime getHandoffAcceptedAt() {
        return handoffAcceptedAt;
    }

    public String getHandoffNotes() {
        return handoffNotes;
    }

    public OffsetDateTime getHandoffRequestedAt() {
        return handoffRequestedAt;
    }

    public String getHandoffRequestedBy() {
        return handoffRequestedBy;
    }

    public OffsetDateTime getHandoffRejectedAt() {
        return handoffRejectedAt;
    }

    public String getHandoffRejectedBy() {
        return handoffRejectedBy;
    }

    public String getHandoffRejectionReason() {
        return handoffRejectionReason;
    }

    public Integer getFuelLevelPercent() {
        return fuelLevelPercent;
    }

    public boolean isTiresChecked() {
        return tiresChecked;
    }

    public boolean isLightsChecked() {
        return lightsChecked;
    }

    public boolean isDocumentsChecked() {
        return documentsChecked;
    }

    public String getChecklistNotes() {
        return checklistNotes;
    }

    public ShiftAttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public Long getCoverageForAgentId() {
        return coverageForAgentId;
    }

    public String getCoverageForAgentName() {
        return coverageForAgentName;
    }

    public String getAttendanceNotes() {
        return attendanceNotes;
    }

    public void update(
            Long agentId,
            String agentName,
            Long vehicleId,
            String vehiclePlate,
            ShiftStatus status,
            OffsetDateTime scheduledStartAt,
            OffsetDateTime scheduledEndAt,
            Long endKm,
            Integer fuelLevelPercent,
            boolean tiresChecked,
            boolean lightsChecked,
            boolean documentsChecked,
            String checklistNotes
    ) {
        // Mantem a edicao operacional do turno sem perder o agente e a viatura vigentes.
        // Tambem consolida o checklist minimo exigido para jornada e liberacao da viatura.
        this.agentId = agentId;
        this.agentName = agentName;
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.status = status;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.endKm = endKm;
        this.fuelLevelPercent = fuelLevelPercent;
        this.tiresChecked = tiresChecked;
        this.lightsChecked = lightsChecked;
        this.documentsChecked = documentsChecked;
        this.checklistNotes = checklistNotes;
    }

    public void beginOperationalTracking(OffsetDateTime startedAt, Long startKm) {
        // Separa o turno planejado do inicio real da jornada e evita ponto artificial no cadastro.
        if (this.checkInAt == null) {
            this.startedAt = startedAt;
            this.checkInAt = startedAt;
            this.startKm = startKm;
        }

        if (this.status == ShiftStatus.PLANNED) {
            this.status = ShiftStatus.ACTIVE;
        }
    }

    public void updateAttendance(
            ShiftAttendanceStatus attendanceStatus,
            Integer lateMinutes,
            Long coverageForAgentId,
            String coverageForAgentName,
            String attendanceNotes
    ) {
        // Centraliza a leitura de presenca do turno para escala, cobertura e atraso.
        this.attendanceStatus = attendanceStatus;
        this.lateMinutes = lateMinutes;
        this.coverageForAgentId = coverageForAgentId;
        this.coverageForAgentName = coverageForAgentName;
        this.attendanceNotes = attendanceNotes;
    }

    public void close(OffsetDateTime checkOutAt, Long endKm) {
        // Consolida o encerramento do turno para jornada e controle de quilometragem.
        this.status = ShiftStatus.CLOSED;
        this.checkOutAt = checkOutAt;
        this.endKm = endKm;
    }

    public void requestHandoff(
            Long handoffFromAgentId,
            String handoffFromAgentName,
            Long handoffToAgentId,
            String handoffToAgentName,
            OffsetDateTime handoffRequestedAt,
            String handoffRequestedBy,
            String handoffNotes
    ) {
        // Registra o pedido de troca sem transferir a responsabilidade antes do aceite do proximo vigilante.
        this.handoffFromAgentId = handoffFromAgentId;
        this.handoffFromAgentName = handoffFromAgentName;
        this.handoffToAgentId = handoffToAgentId;
        this.handoffToAgentName = handoffToAgentName;
        this.handoffRequestedAt = handoffRequestedAt;
        this.handoffRequestedBy = handoffRequestedBy;
        this.handoffAcceptedAt = null;
        this.handoffRejectedAt = null;
        this.handoffRejectedBy = null;
        this.handoffRejectionReason = null;
        this.handoffNotes = handoffNotes;
        this.status = ShiftStatus.HANDOFF_PENDING;
    }

    public void acceptHandoff(OffsetDateTime handoffAcceptedAt, String handoffNotes) {
        // So transfere a responsabilidade depois do aceite explicito do vigilante que vai assumir.
        this.handoffAcceptedAt = handoffAcceptedAt;
        this.handoffNotes = handoffNotes;
        this.agentId = handoffToAgentId;
        this.agentName = handoffToAgentName;
        this.status = ShiftStatus.HANDOFF;
    }

    public void rejectHandoff(OffsetDateTime rejectedAt, String rejectedBy, String rejectionReason) {
        // Mantem o agente atual no turno quando a troca for recusada e deixa a trilha do motivo.
        this.handoffRejectedAt = rejectedAt;
        this.handoffRejectedBy = rejectedBy;
        this.handoffRejectionReason = rejectionReason;
        this.status = ShiftStatus.ACTIVE;
    }

    public void applyCoverage(Long replacementAgentId, String replacementAgentName, String attendanceNotes) {
        // Reatribui o turno para a cobertura mantendo o vigilante originalmente afetado na trilha.
        this.coverageForAgentId = this.agentId;
        this.coverageForAgentName = this.agentName;
        this.agentId = replacementAgentId;
        this.agentName = replacementAgentName;
        this.attendanceStatus = ShiftAttendanceStatus.COVERED;
        this.attendanceNotes = attendanceNotes;
        this.status = ShiftStatus.ACTIVE;
    }

    public void setAttendanceManually(ShiftAttendanceStatus attendanceStatus, Integer lateMinutes, String attendanceNotes) {
        // Permite que a supervisao marque atraso, falta ou normalidade antes do check-in automatico.
        this.attendanceStatus = attendanceStatus;
        this.lateMinutes = lateMinutes;
        this.attendanceNotes = attendanceNotes;
    }
}
