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

    protected Shift() {
    }

    public Shift(Long agentId, String agentName, Long vehicleId, String vehiclePlate, ShiftStatus status, OffsetDateTime startedAt, OffsetDateTime scheduledEndAt) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.status = status;
        this.startedAt = startedAt;
        this.scheduledEndAt = scheduledEndAt;
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

    public void update(Long agentId, String agentName, Long vehicleId, String vehiclePlate, ShiftStatus status, OffsetDateTime scheduledEndAt) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.status = status;
        this.scheduledEndAt = scheduledEndAt;
    }
}
