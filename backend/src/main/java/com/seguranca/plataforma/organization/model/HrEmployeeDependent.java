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
@Table(name = "hr_employee_dependents")
public class HrEmployeeDependent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column
    private String cpf;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyRelationshipType relationship;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected HrEmployeeDependent() {
    }

    public HrEmployeeDependent(Long employeeId, String fullName, String cpf, String phoneNumber, FamilyRelationshipType relationship, String notes) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.cpf = cpf;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
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

    public Long getEmployeeId() {
        return employeeId;
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

    public String getNotes() {
        return notes;
    }
}


