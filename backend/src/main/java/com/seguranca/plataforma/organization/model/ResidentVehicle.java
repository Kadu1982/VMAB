package com.seguranca.plataforma.organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "resident_vehicles")
public class ResidentVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(nullable = false)
    private String plate;

    @Column
    private String model;

    @Column
    private String color;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ResidentVehicle() {
    }

    public ResidentVehicle(Long residentId, String plate, String model, String color, boolean active, String notes) {
        this.residentId = residentId;
        this.plate = plate;
        this.model = model;
        this.color = color;
        this.active = active;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public Long getResidentId() {
        return residentId;
    }

    public String getPlate() {
        return plate;
    }

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public boolean isActive() {
        return active;
    }

    public String getNotes() {
        return notes;
    }
}


