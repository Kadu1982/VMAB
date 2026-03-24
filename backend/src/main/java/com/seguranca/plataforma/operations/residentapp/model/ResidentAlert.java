package com.seguranca.plataforma.operations.residentapp.model;

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
@Table(name = "resident_alerts")
public class ResidentAlert {
    // Registro de alerta do morador com trilha completa de atendimento.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "resident_name", nullable = false)
    private String residentName;

    @Column(name = "resident_phone_number", nullable = false)
    private String residentPhoneNumber;

    @Column(name = "resident_address", nullable = false)
    private String residentAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResidentAlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResidentAlertStatus status;

    @Column(nullable = false)
    private OffsetDateTime openedAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "acknowledged_at")
    private OffsetDateTime acknowledgedAt;

    @Column(name = "dispatched_at")
    private OffsetDateTime dispatchedAt;

    @Column(name = "on_site_at")
    private OffsetDateTime onSiteAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(length = 1000)
    private String notes;

    @Column(name = "acknowledgment_notes", length = 500)
    private String acknowledgmentNotes;

    @Column(name = "dispatch_notes", length = 500)
    private String dispatchNotes;

    @Column(name = "arrival_notes", length = 500)
    private String arrivalNotes;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(name = "assigned_agent_name")
    private String assignedAgentName;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_plate")
    private String vehiclePlate;

    protected ResidentAlert() {
    }

    public ResidentAlert(
            Long residentId,
            String residentName,
            String residentPhoneNumber,
            String residentAddress,
            ResidentAlertType type,
            ResidentAlertStatus status,
            OffsetDateTime openedAt,
            OffsetDateTime updatedAt,
            Double latitude,
            Double longitude,
            String notes
    ) {
        this.residentId = residentId;
        this.residentName = residentName;
        this.residentPhoneNumber = residentPhoneNumber;
        this.residentAddress = residentAddress;
        this.type = type;
        this.status = status;
        this.openedAt = openedAt;
        this.updatedAt = updatedAt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public Long getResidentId() {
        return residentId;
    }

    public String getResidentName() {
        return residentName;
    }

    public String getResidentPhoneNumber() {
        return residentPhoneNumber;
    }

    public String getResidentAddress() {
        return residentAddress;
    }

    public ResidentAlertType getType() {
        return type;
    }

    public ResidentAlertStatus getStatus() {
        return status;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public OffsetDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public OffsetDateTime getOnSiteAt() {
        return onSiteAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getNotes() {
        return notes;
    }

    public String getAcknowledgmentNotes() {
        return acknowledgmentNotes;
    }

    public String getDispatchNotes() {
        return dispatchNotes;
    }

    public String getArrivalNotes() {
        return arrivalNotes;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public String getCancellationReason() {
        return cancellationReason;
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

    public void acknowledge(OffsetDateTime acknowledgedAt, String acknowledgmentNotes) {
        this.status = ResidentAlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = acknowledgedAt;
        this.acknowledgmentNotes = acknowledgmentNotes;
        this.updatedAt = acknowledgedAt;
    }

    public void dispatch(
            Long assignedAgentId,
            String assignedAgentName,
            Long vehicleId,
            String vehiclePlate,
            OffsetDateTime dispatchedAt,
            String dispatchNotes
    ) {
        this.assignedAgentId = assignedAgentId;
        this.assignedAgentName = assignedAgentName;
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.dispatchedAt = dispatchedAt;
        this.dispatchNotes = dispatchNotes;
        this.status = ResidentAlertStatus.DISPATCHED;
        this.updatedAt = dispatchedAt;
    }

    public void markOnSite(OffsetDateTime onSiteAt, String arrivalNotes) {
        this.onSiteAt = onSiteAt;
        this.arrivalNotes = arrivalNotes;
        this.status = ResidentAlertStatus.ON_SITE;
        this.updatedAt = onSiteAt;
    }

    public void resolve(OffsetDateTime resolvedAt, String resolutionNotes) {
        this.resolvedAt = resolvedAt;
        this.resolutionNotes = resolutionNotes;
        this.status = ResidentAlertStatus.RESOLVED;
        this.updatedAt = resolvedAt;
    }

    public void cancel(OffsetDateTime cancelledAt, String cancellationReason) {
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.status = ResidentAlertStatus.CANCELLED;
        this.updatedAt = cancelledAt;
    }
}
