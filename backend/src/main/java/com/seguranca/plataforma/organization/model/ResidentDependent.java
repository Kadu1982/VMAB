package com.seguranca.plataforma.organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "resident_dependents")
public class ResidentDependent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column
    private String cpf;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyRelationshipType relationship;

    @Column(name = "access_enabled", nullable = false)
    private boolean accessEnabled;

    @Column(name = "app_enabled", nullable = false)
    private boolean appEnabled;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ResidentDependent() {
    }

    public ResidentDependent(
            Long residentId,
            String fullName,
            String cpf,
            String phoneNumber,
            FamilyRelationshipType relationship,
            boolean accessEnabled,
            boolean appEnabled,
            String notes
    ) {
        this.residentId = residentId;
        this.fullName = fullName;
        this.cpf = cpf;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.accessEnabled = accessEnabled;
        this.appEnabled = appEnabled;
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

    public String getFullName() {
        return fullName;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public FamilyRelationshipType getRelationship() {
        return relationship;
    }

    public boolean isAccessEnabled() {
        return accessEnabled;
    }

    public boolean isAppEnabled() {
        return appEnabled;
    }

    public String getNotes() {
        return notes;
    }
}


