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
@Table(name = "business_sectors")
public class BusinessSector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_unit_id", nullable = false)
    private Long businessUnitId;

    @Column(nullable = false)
    private String name;

    @Column
    private String code;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected BusinessSector() {
    }

    public BusinessSector(Long businessUnitId, String name, String code, boolean active, String notes) {
        this.businessUnitId = businessUnitId;
        this.name = name;
        this.code = code;
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

    public Long getBusinessUnitId() {
        return businessUnitId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public boolean isActive() {
        return active;
    }

    public String getNotes() {
        return notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(Long businessUnitId, String name, String code, boolean active, String notes) {
        this.businessUnitId = businessUnitId;
        this.name = name;
        this.code = code;
        this.active = active;
        this.notes = notes;
    }
}


