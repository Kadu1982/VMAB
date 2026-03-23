package com.seguranca.plataforma.operations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    protected Vehicle() {
    }

    public Vehicle(String plate, String model, long currentKm, long nextMaintenanceKm, VehicleStatus status) {
        this.plate = plate;
        this.model = model;
        this.currentKm = currentKm;
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.status = status;
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

    public void update(String plate, String model, long currentKm, long nextMaintenanceKm, VehicleStatus status) {
        this.plate = plate;
        this.model = model;
        this.currentKm = currentKm;
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.status = status;
    }
}
