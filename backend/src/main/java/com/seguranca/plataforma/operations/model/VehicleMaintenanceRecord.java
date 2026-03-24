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

    @Column(name = "maintenance_code", nullable = false, unique = true)
    private String maintenanceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleMaintenanceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleMaintenancePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleMaintenanceStatus status;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "km_at_service")
    private Long kmAtService;

    @Column(name = "next_maintenance_km")
    private Long nextMaintenanceKm;

    @Column(name = "cost_amount", precision = 12, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(length = 1000, nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean resolved;

    protected VehicleMaintenanceRecord() {
    }

    public VehicleMaintenanceRecord(
            Long vehicleId,
            String vehiclePlate,
            String maintenanceCode,
            VehicleMaintenanceType type,
            VehicleMaintenancePriority priority,
            VehicleMaintenanceStatus status,
            OffsetDateTime openedAt,
            OffsetDateTime completedAt,
            LocalDate serviceDate,
            LocalDate dueDate,
            Long kmAtService,
            Long nextMaintenanceKm,
            BigDecimal costAmount,
            String supplierName,
            String resolutionNotes,
            String description,
            boolean resolved
    ) {
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.maintenanceCode = maintenanceCode;
        this.type = type;
        this.priority = priority;
        this.status = status;
        this.openedAt = openedAt;
        this.completedAt = completedAt;
        this.serviceDate = serviceDate;
        this.dueDate = dueDate;
        this.kmAtService = kmAtService;
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.costAmount = costAmount;
        this.supplierName = supplierName;
        this.resolutionNotes = resolutionNotes;
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

    public String getMaintenanceCode() {
        return maintenanceCode;
    }

    public VehicleMaintenanceType getType() {
        return type;
    }

    public VehicleMaintenancePriority getPriority() {
        return priority;
    }

    public VehicleMaintenanceStatus getStatus() {
        return status;
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

    public LocalDate getDueDate() {
        return dueDate;
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

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public String getDescription() {
        return description;
    }

    public boolean isResolved() {
        return resolved;
    }

    public String getWorkOrderCode() {
        // Gera um codigo humano para a ordem de servico sem depender de dados externos.
        return maintenanceCode != null && !maintenanceCode.isBlank() ? maintenanceCode : "OS-RASCUNHO";
    }

    public VehicleMaintenanceLifecycleStatus getLifecycleStatus() {
        // Deixa o ciclo da OS explicito para dashboard, relatorios e futuras regras operacionais.
        if (status == VehicleMaintenanceStatus.CANCELLED) {
            return VehicleMaintenanceLifecycleStatus.CANCELLED;
        }

        if (resolved || status == VehicleMaintenanceStatus.COMPLETED) {
            return VehicleMaintenanceLifecycleStatus.RESOLVED;
        }

        if (status == VehicleMaintenanceStatus.WAITING_PARTS || status == VehicleMaintenanceStatus.IN_PROGRESS) {
            return VehicleMaintenanceLifecycleStatus.BLOCKING;
        }

        if (type == VehicleMaintenanceType.DOCUMENTATION) {
            return VehicleMaintenanceLifecycleStatus.DOCUMENTATION;
        }

        return VehicleMaintenanceLifecycleStatus.OPEN;
    }

    public String getLifecycleLabel() {
        // Traduz o ciclo da OS para um texto operacional em portugues do Brasil.
        return switch (getLifecycleStatus()) {
            case RESOLVED -> "Resolvida";
            case DOCUMENTATION -> "Documentacao";
            case BLOCKING -> "Bloqueante";
            case CANCELLED -> "Cancelada";
            case OPEN -> "Aberta";
        };
    }

    public boolean isBlockingOrder() {
        // Ordens corretivas e de inspecao abertas derrubam a disponibilidade da viatura.
        return getLifecycleStatus() == VehicleMaintenanceLifecycleStatus.BLOCKING;
    }

    public void update(
            VehicleMaintenanceType type,
            VehicleMaintenancePriority priority,
            VehicleMaintenanceStatus status,
            OffsetDateTime completedAt,
            LocalDate serviceDate,
            LocalDate dueDate,
            Long kmAtService,
            Long nextMaintenanceKm,
            BigDecimal costAmount,
            String supplierName,
            String resolutionNotes,
            String description,
            boolean resolved
    ) {
        // Mantem o historico auditavel da manutencao sem perder a referencia original de abertura.
        this.type = type;
        this.priority = priority;
        this.status = status;
        this.completedAt = completedAt;
        this.serviceDate = serviceDate;
        this.dueDate = dueDate;
        this.kmAtService = kmAtService;
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.costAmount = costAmount;
        this.supplierName = supplierName;
        this.resolutionNotes = resolutionNotes;
        this.description = description;
        this.resolved = resolved;
    }
}
