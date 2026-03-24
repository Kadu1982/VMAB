package com.seguranca.plataforma.operations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicle_maintenance_records")
public class VehicleMaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "vehicle_plate", nullable = false)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleMaintenanceType type;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "km_at_service")
    private Long kmAtService;

    @Column(name = "next_maintenance_km")
    private Long nextMaintenanceKm;

    @Column(name = "cost_amount", precision = 12, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(length = 1000, nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean resolved;

    protected VehicleMaintenanceRecord() {
    }

    public VehicleMaintenanceRecord(
            Long vehicleId,
            String vehiclePlate,
            VehicleMaintenanceType type,
            OffsetDateTime openedAt,
            OffsetDateTime completedAt,
            LocalDate serviceDate,
            Long kmAtService,
            Long nextMaintenanceKm,
            BigDecimal costAmount,
            String supplierName,
            String description,
            boolean resolved
    ) {
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.type = type;
        this.openedAt = openedAt;
        this.completedAt = completedAt;
        this.serviceDate = serviceDate;
        this.kmAtService = kmAtService;
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.costAmount = costAmount;
        this.supplierName = supplierName;
        this.description = description;
        this.resolved = resolved;
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public VehicleMaintenanceType getType() {
        return type;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public Long getKmAtService() {
        return kmAtService;
    }

    public Long getNextMaintenanceKm() {
        return nextMaintenanceKm;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void update(
            VehicleMaintenanceType type,
            OffsetDateTime completedAt,
            LocalDate serviceDate,
            Long kmAtService,
            Long nextMaintenanceKm,
            BigDecimal costAmount,
            String supplierName,
            String description,
            boolean resolved
    ) {
        // Mantem o historico auditavel da manutencao sem perder a referencia original de abertura.
        this.type = type;
        this.completedAt = completedAt;
        this.serviceDate = serviceDate;
        this.kmAtService = kmAtService;
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.costAmount = costAmount;
        this.supplierName = supplierName;
        this.description = description;
        this.resolved = resolved;
    }
}
