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
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(name = "resident_name", nullable = false)
    private String residentName;

    @Column(nullable = false)
    private String address;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "assigned_agent_name")
    private String assignedAgentName;

    @Column(name = "vehicle_plate")
    private String vehiclePlate;

    protected Incident() {
    }

    public Incident(IncidentType type, IncidentPriority priority, IncidentStatus status, String residentName, String address, OffsetDateTime openedAt, String assignedAgentName, String vehiclePlate) {
        this.type = type;
        this.priority = priority;
        this.status = status;
        this.residentName = residentName;
        this.address = address;
        this.openedAt = openedAt;
        this.assignedAgentName = assignedAgentName;
        this.vehiclePlate = vehiclePlate;
    }

    public Long getId() {
        return id;
    }

    public IncidentType getType() {
        return type;
    }

    public IncidentPriority getPriority() {
        return priority;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public String getResidentName() {
        return residentName;
    }

    public String getAddress() {
        return address;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public String getAssignedAgentName() {
        return assignedAgentName;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void update(IncidentType type, IncidentPriority priority, IncidentStatus status, String residentName, String address, String assignedAgentName, String vehiclePlate) {
        this.type = type;
        this.priority = priority;
        this.status = status;
        this.residentName = residentName;
        this.address = address;
        this.assignedAgentName = assignedAgentName;
        this.vehiclePlate = vehiclePlate;
    }
}
