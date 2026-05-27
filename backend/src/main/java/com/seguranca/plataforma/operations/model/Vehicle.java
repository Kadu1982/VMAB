package com.seguranca.plataforma.operations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String plate;

    @Column(nullable = false)
    private String model;

    @Column(name = "current_km", nullable = false)
    private long currentKm;

    @Column(name = "next_maintenance_km", nullable = false)
    private long nextMaintenanceKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;

    @Column(name = "ipva_expiry")
    private LocalDate ipvaExpiry;

    @Column(name = "licensing_expiry")
    private LocalDate licensingExpiry;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "last_maintenance_at")
    private LocalDate lastMaintenanceAt;

    @Column(name = "maintenance_notes", length = 500)
    private String maintenanceNotes;

    protected Vehicle() {
    }

    public Vehicle(
            String plate,
            String model,
            long currentKm,
            long nextMaintenanceKm,
            VehicleStatus status,
            LocalDate ipvaExpiry,
            LocalDate licensingExpiry,
            LocalDate insuranceExpiry,
            LocalDate lastMaintenanceAt,
            String maintenanceNotes
    ) {
        this.plate = plate;
        this.model = model;
        this.currentKm = currentKm;
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.status = status;
        this.ipvaExpiry = ipvaExpiry;
        this.licensingExpiry = licensingExpiry;
        this.insuranceExpiry = insuranceExpiry;
        this.lastMaintenanceAt = lastMaintenanceAt;
        this.maintenanceNotes = maintenanceNotes;
    }

    public Long getId() {
        return id;
    }

    public String getPlate() {
        return plate;
    }

    public String getModel() {
        return model;
    }

    public long getCurrentKm() {
        return currentKm;
    }

    public long getNextMaintenanceKm() {
        return nextMaintenanceKm;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public LocalDate getIpvaExpiry() {
        return ipvaExpiry;
    }

    public LocalDate getLicensingExpiry() {
        return licensingExpiry;
    }

    public LocalDate getInsuranceExpiry() {
        return insuranceExpiry;
    }

    public LocalDate getLastMaintenanceAt() {
        return lastMaintenanceAt;
    }

    public String getMaintenanceNotes() {
        return maintenanceNotes;
    }

    public long getRemainingMaintenanceKm() {
        // ExpÃµe a distancia ate a proxima revisao para relatorios e regras operacionais.
        return nextMaintenanceKm - currentKm;
    }

    public boolean isMaintenanceDue() {
        // Quando a margem zera ou fica negativa, a viatura ja não pode ser tratada como pronta.
        return getRemainingMaintenanceKm() <= 0;
    }

    public boolean isMaintenanceDueSoon(long alertThresholdKm) {
        // Usa uma margem objetiva para alertar a base antes da manutenção vencer de fato.
        return getRemainingMaintenanceKm() <= alertThresholdKm;
    }

    public boolean hasDocumentAlert(LocalDate referenceDate, int alertWindowDays) {
        // Centraliza o criterio de alerta documental para evitar logica duplicada no backend.
        LocalDate threshold = referenceDate.plusDays(alertWindowDays);
        return (ipvaExpiry != null && !ipvaExpiry.isAfter(threshold))
                || (licensingExpiry != null && !licensingExpiry.isAfter(threshold))
                || (insuranceExpiry != null && !insuranceExpiry.isAfter(threshold));
    }

    public boolean isOperationallyReady() {
        // Apenas status liberados para uso em turno sao considerados prontos para operação.
        return status == VehicleStatus.AVAILABLE || status == VehicleStatus.IN_OPERATION;
    }

    public void update(
            String plate,
            String model,
            long currentKm,
            long nextMaintenanceKm,
            VehicleStatus status,
            LocalDate ipvaExpiry,
            LocalDate licensingExpiry,
            LocalDate insuranceExpiry,
            LocalDate lastMaintenanceAt,
            String maintenanceNotes
    ) {
        this.plate = plate;
        this.model = model;
        this.currentKm = currentKm;
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.status = status;
        this.ipvaExpiry = ipvaExpiry;
        this.licensingExpiry = licensingExpiry;
        this.insuranceExpiry = insuranceExpiry;
        this.lastMaintenanceAt = lastMaintenanceAt;
        this.maintenanceNotes = maintenanceNotes;
    }
}


