package com.seguranca.plataforma.hr.model;

import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.AgentStatus;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "hr_employees")
public class HrEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true)
    private String employeeCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HrEmployeeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HrEmployeeStatus status;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "cnh_category")
    private String cnhCategory;

    @Column(name = "cnh_expiry")
    private LocalDate cnhExpiry;

    @Column(name = "medical_exam_expiry")
    private LocalDate medicalExamExpiry;

    @Column(name = "training_expiry")
    private LocalDate trainingExpiry;

    @Column(name = "training_notes", length = 500)
    private String trainingNotes;

    @Column(name = "document_notes", length = 500)
    private String documentNotes;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "linked_agent_id", unique = true)
    private Long linkedAgentId;

    @Column(name = "linked_app_user_id", unique = true)
    private Long linkedAppUserId;

    @Column(name = "point_enabled", nullable = false)
    private boolean pointEnabled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_check_in_at")
    private OffsetDateTime lastCheckInAt;

    @Column(name = "last_check_out_at")
    private OffsetDateTime lastCheckOutAt;

    @Column(name = "last_check_in_device")
    private String lastCheckInDevice;

    @Column(name = "last_check_out_device")
    private String lastCheckOutDevice;

    @Column(name = "last_check_in_latitude")
    private Double lastCheckInLatitude;

    @Column(name = "last_check_in_longitude")
    private Double lastCheckInLongitude;

    @Column(name = "last_check_out_latitude")
    private Double lastCheckOutLatitude;

    @Column(name = "last_check_out_longitude")
    private Double lastCheckOutLongitude;

    @Column(name = "last_point_notes", length = 500)
    private String lastPointNotes;

    protected HrEmployee() {
    }

    public HrEmployee(
            String employeeCode,
            String fullName,
            HrEmployeeCategory category,
            HrEmployeeStatus status,
            String documentNumber,
            String phoneNumber,
            String email,
            String photoUrl,
            String cnhCategory,
            LocalDate cnhExpiry,
            LocalDate medicalExamExpiry,
            LocalDate trainingExpiry,
            String trainingNotes,
            String documentNotes,
            LocalDate hireDate,
            LocalDate terminationDate,
            Long linkedAgentId,
            Long linkedAppUserId,
            boolean pointEnabled
    ) {
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.category = category;
        this.status = status;
        this.documentNumber = documentNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.photoUrl = photoUrl;
        this.cnhCategory = cnhCategory;
        this.cnhExpiry = cnhExpiry;
        this.medicalExamExpiry = medicalExamExpiry;
        this.trainingExpiry = trainingExpiry;
        this.trainingNotes = trainingNotes;
        this.documentNotes = documentNotes;
        this.hireDate = hireDate;
        this.terminationDate = terminationDate;
        this.linkedAgentId = linkedAgentId;
        this.linkedAppUserId = linkedAppUserId;
        this.pointEnabled = pointEnabled;
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

    public static HrEmployee fromAgent(Long agentId, Agent agent) {
        HrEmployee employee = new HrEmployee(
                agent.getBadgeCode(),
                agent.getFullName(),
                HrEmployeeCategory.VIGILANTE,
                agent.getStatus() == AgentStatus.BLOCKED ? HrEmployeeStatus.BLOCKED : HrEmployeeStatus.ACTIVE,
                null,
                null,
                null,
                agent.getPhotoUrl(),
                agent.getCnhCategory(),
                agent.getCnhExpiry(),
                agent.getMedicalExamExpiry(),
                null,
                null,
                agent.getDocumentNotes(),
                null,
                null,
                agentId,
                null,
                agent.getStatus() == AgentStatus.ACTIVE
        );
        employee.lastPointNotes = agent.getDocumentNotes();
        return employee;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public HrEmployeeCategory getCategory() {
        return category;
    }

    public HrEmployeeStatus getStatus() {
        return status;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getCnhCategory() {
        return cnhCategory;
    }

    public LocalDate getCnhExpiry() {
        return cnhExpiry;
    }

    public LocalDate getMedicalExamExpiry() {
        return medicalExamExpiry;
    }

    public LocalDate getTrainingExpiry() {
        return trainingExpiry;
    }

    public String getTrainingNotes() {
        return trainingNotes;
    }

    public String getDocumentNotes() {
        return documentNotes;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public Long getLinkedAgentId() {
        return linkedAgentId;
    }

    public Long getLinkedAppUserId() {
        return linkedAppUserId;
    }

    public boolean isPointEnabled() {
        return pointEnabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getLastCheckInAt() {
        return lastCheckInAt;
    }

    public OffsetDateTime getLastCheckOutAt() {
        return lastCheckOutAt;
    }

    public String getLastCheckInDevice() {
        return lastCheckInDevice;
    }

    public String getLastCheckOutDevice() {
        return lastCheckOutDevice;
    }

    public Double getLastCheckInLatitude() {
        return lastCheckInLatitude;
    }

    public Double getLastCheckInLongitude() {
        return lastCheckInLongitude;
    }

    public Double getLastCheckOutLatitude() {
        return lastCheckOutLatitude;
    }

    public Double getLastCheckOutLongitude() {
        return lastCheckOutLongitude;
    }

    public String getLastPointNotes() {
        return lastPointNotes;
    }

    public void updateProfile(
            String employeeCode,
            String fullName,
            HrEmployeeCategory category,
            HrEmployeeStatus status,
            String documentNumber,
            String phoneNumber,
            String email,
            String photoUrl,
            String cnhCategory,
            LocalDate cnhExpiry,
            LocalDate medicalExamExpiry,
            LocalDate trainingExpiry,
            String trainingNotes,
            String documentNotes,
            LocalDate hireDate,
            LocalDate terminationDate,
            Long linkedAgentId,
            Long linkedAppUserId,
            boolean pointEnabled
    ) {
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.category = category;
        this.status = status;
        this.documentNumber = documentNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.photoUrl = photoUrl;
        this.cnhCategory = cnhCategory;
        this.cnhExpiry = cnhExpiry;
        this.medicalExamExpiry = medicalExamExpiry;
        this.trainingExpiry = trainingExpiry;
        this.trainingNotes = trainingNotes;
        this.documentNotes = documentNotes;
        this.hireDate = hireDate;
        this.terminationDate = terminationDate;
        this.linkedAgentId = linkedAgentId;
        this.linkedAppUserId = linkedAppUserId;
        this.pointEnabled = pointEnabled;
    }

    public void applyAgentSnapshot(Agent agent) {
        this.fullName = agent.getFullName();
        this.photoUrl = agent.getPhotoUrl();
        this.cnhCategory = agent.getCnhCategory();
        this.cnhExpiry = agent.getCnhExpiry();
        this.medicalExamExpiry = agent.getMedicalExamExpiry();
        this.trainingExpiry = agent.getWorkExamsExpiry();
        this.documentNotes = agent.getDocumentNotes();
        if (agent.getId() != null) {
            this.linkedAgentId = agent.getId();
        }
        this.category = HrEmployeeCategory.VIGILANTE;
        if (this.status != HrEmployeeStatus.TERMINATED) {
            this.status = agent.getStatus() == AgentStatus.BLOCKED ? HrEmployeeStatus.BLOCKED : HrEmployeeStatus.ACTIVE;
        }
        this.pointEnabled = this.status == HrEmployeeStatus.ACTIVE;
    }

    public void terminate() {
        this.status = HrEmployeeStatus.TERMINATED;
        this.terminationDate = LocalDate.now(ZoneOffset.UTC);
        this.pointEnabled = false;
    }

    public void recordCheckIn(OffsetDateTime occurredAt, String deviceLabel, Double latitude, Double longitude, String note) {
        this.lastCheckInAt = occurredAt;
        this.lastCheckInDevice = deviceLabel;
        this.lastCheckInLatitude = latitude;
        this.lastCheckInLongitude = longitude;
        this.lastPointNotes = note;
    }

    public void recordCheckOut(OffsetDateTime occurredAt, String deviceLabel, Double latitude, Double longitude, String note) {
        this.lastCheckOutAt = occurredAt;
        this.lastCheckOutDevice = deviceLabel;
        this.lastCheckOutLatitude = latitude;
        this.lastCheckOutLongitude = longitude;
        this.lastPointNotes = note;
    }
}
