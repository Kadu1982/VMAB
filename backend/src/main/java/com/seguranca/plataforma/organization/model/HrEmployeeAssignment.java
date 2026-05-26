package com.seguranca.plataforma.organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "hr_employee_assignments")
public class HrEmployeeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "business_unit_id", nullable = false)
    private Long businessUnitId;

    @Column(name = "business_sector_id")
    private Long businessSectorId;

    @Column(name = "role_title")
    private String roleTitle;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected HrEmployeeAssignment() {
    }

    public HrEmployeeAssignment(
            Long employeeId,
            Long businessUnitId,
            Long businessSectorId,
            String roleTitle,
            LocalDate startDate,
            LocalDate endDate,
            boolean active,
            String notes
    ) {
        this.employeeId = employeeId;
        this.businessUnitId = businessUnitId;
        this.businessSectorId = businessSectorId;
        this.roleTitle = roleTitle;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getBusinessUnitId() {
        return businessUnitId;
    }

    public Long getBusinessSectorId() {
        return businessSectorId;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public String getNotes() {
        return notes;
    }
}
