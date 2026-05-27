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

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(name = "assigned_agent_name")
    private String assignedAgentName;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_plate")
    private String vehiclePlate;

    @Column(name = "dispatched_at")
    private OffsetDateTime dispatchedAt;

    @Column(name = "on_site_at")
    private OffsetDateTime onSiteAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "dispatch_notes", length = 500)
    private String dispatchNotes;

    @Column(name = "arrival_notes", length = 500)
    private String arrivalNotes;

    @Column(name = "closure_notes", length = 1000)
    private String closureNotes;

    protected Incident() {
    }

    public Incident(
            IncidentType type,
            IncidentPriority priority,
            IncidentStatus status,
            String residentName,
            String address,
            OffsetDateTime openedAt,
            Long assignedAgentId,
            String assignedAgentName,
            Long vehicleId,
            String vehiclePlate,
            OffsetDateTime dispatchedAt,
            OffsetDateTime onSiteAt,
            OffsetDateTime closedAt,
            String dispatchNotes,
            String arrivalNotes,
            String closureNotes
    ) {
        this.type = type;
        this.priority = priority;
        this.status = status;
        this.residentName = residentName;
        this.address = address;
        this.openedAt = openedAt;
        this.assignedAgentId = assignedAgentId;
        this.assignedAgentName = assignedAgentName;
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.dispatchedAt = dispatchedAt;
        this.onSiteAt = onSiteAt;
        this.closedAt = closedAt;
        this.dispatchNotes = dispatchNotes;
        this.arrivalNotes = arrivalNotes;
        this.closureNotes = closureNotes;
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

    public Long getAssignedAgentId() {
        return assignedAgentId;
    }

    public String getAssignedAgentName() {
        return assignedAgentName;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public OffsetDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public OffsetDateTime getOnSiteAt() {
        return onSiteAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public String getDispatchNotes() {
        return dispatchNotes;
    }

    public String getArrivalNotes() {
        return arrivalNotes;
    }

    public String getClosureNotes() {
        return closureNotes;
    }

    public void update(
            IncidentType type,
            IncidentPriority priority,
            IncidentStatus status,
            String residentName,
            String address,
            Long assignedAgentId,
            String assignedAgentName,
            Long vehicleId,
            String vehiclePlate
    ) {
        this.type = type;
        this.priority = priority;
        this.status = status;
        this.residentName = residentName;
        this.address = address;
        this.assignedAgentId = assignedAgentId;
        this.assignedAgentName = assignedAgentName;
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
    }

    public void dispatch(
            Long assignedAgentId,
            String assignedAgentName,
            Long vehicleId,
            String vehiclePlate,
            OffsetDateTime dispatchedAt,
            String dispatchNotes
    ) {
        // Formaliza o despacho da ocorrência com quem recebeu a ordem e qual viatura foi enviada.
        this.assignedAgentId = assignedAgentId;
        this.assignedAgentName = assignedAgentName;
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.dispatchedAt = dispatchedAt;
        this.dispatchNotes = dispatchNotes;
        this.status = IncidentStatus.DISPATCHED;
    }

    public void markOnSite(OffsetDateTime onSiteAt, String arrivalNotes) {
        // Registra a chegada no local sem perder o historico do despacho anterior.
        this.onSiteAt = onSiteAt;
        this.arrivalNotes = arrivalNotes;
        this.status = IncidentStatus.ON_SITE;
    }

    public void close(OffsetDateTime closedAt, String closureNotes) {
        // Encerra a ocorrência com trilha temporal e observacao de fechamento.
        this.closedAt = closedAt;
        this.closureNotes = closureNotes;
        this.status = IncidentStatus.CLOSED;
    }
}


