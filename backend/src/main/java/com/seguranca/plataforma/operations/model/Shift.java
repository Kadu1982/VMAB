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

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

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

    protected Shift() {
    }

    public Shift(
            Long agentId,
            String agentName,
            Long vehicleId,
            String vehiclePlate,
            ShiftStatus status,
            OffsetDateTime startedAt,
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
        this.scheduledEndAt = scheduledEndAt;
        this.checkInAt = checkInAt;
        this.startKm = startKm;
        this.fuelLevelPercent = fuelLevelPercent;
        this.tiresChecked = tiresChecked;
        this.lightsChecked = lightsChecked;
        this.documentsChecked = documentsChecked;
        this.checklistNotes = checklistNotes;
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

    public void update(
            Long agentId,
            String agentName,
            Long vehicleId,
            String vehiclePlate,
            ShiftStatus status,
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
        this.scheduledEndAt = scheduledEndAt;
        this.endKm = endKm;
        this.fuelLevelPercent = fuelLevelPercent;
        this.tiresChecked = tiresChecked;
        this.lightsChecked = lightsChecked;
        this.documentsChecked = documentsChecked;
        this.checklistNotes = checklistNotes;
    }

    public void close(OffsetDateTime checkOutAt, Long endKm) {
        // Consolida o encerramento do turno para jornada e controle de quilometragem.
        this.status = ShiftStatus.CLOSED;
        this.checkOutAt = checkOutAt;
        this.endKm = endKm;
    }

    public void registerHandoff(
            Long handoffFromAgentId,
            String handoffFromAgentName,
            Long handoffToAgentId,
            String handoffToAgentName,
            OffsetDateTime handoffAcceptedAt,
            String handoffNotes
    ) {
        // Registra a passagem formal de responsabilidade entre vigilantes no mesmo turno.
        this.handoffFromAgentId = handoffFromAgentId;
        this.handoffFromAgentName = handoffFromAgentName;
        this.handoffToAgentId = handoffToAgentId;
        this.handoffToAgentName = handoffToAgentName;
        this.handoffAcceptedAt = handoffAcceptedAt;
        this.handoffNotes = handoffNotes;
        this.agentId = handoffToAgentId;
        this.agentName = handoffToAgentName;
        this.status = ShiftStatus.HANDOFF;
    }
}
