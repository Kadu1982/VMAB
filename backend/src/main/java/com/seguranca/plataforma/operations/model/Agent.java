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
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "badge_code", nullable = false, unique = true)
    private String badgeCode;

    @Column(name = "cnh_category", nullable = false)
    private String cnhCategory;

    @Column(name = "cnh_expiry", nullable = false)
    private LocalDate cnhExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "medical_exam_expiry")
    private LocalDate medicalExamExpiry;

    @Column(name = "work_exams_expiry")
    private LocalDate workExamsExpiry;

    @Column(name = "document_notes", length = 500)
    private String documentNotes;

    protected Agent() {
    }

    public Agent(
            String fullName,
            String badgeCode,
            String cnhCategory,
            LocalDate cnhExpiry,
            AgentStatus status,
            String photoUrl,
            LocalDate medicalExamExpiry,
            LocalDate workExamsExpiry,
            String documentNotes
    ) {
        this.fullName = fullName;
        this.badgeCode = badgeCode;
        this.cnhCategory = cnhCategory;
        this.cnhExpiry = cnhExpiry;
        this.status = status;
        this.photoUrl = photoUrl;
        this.medicalExamExpiry = medicalExamExpiry;
        this.workExamsExpiry = workExamsExpiry;
        this.documentNotes = documentNotes;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBadgeCode() {
        return badgeCode;
    }

    public String getCnhCategory() {
        return cnhCategory;
    }

    public LocalDate getCnhExpiry() {
        return cnhExpiry;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public LocalDate getMedicalExamExpiry() {
        return medicalExamExpiry;
    }

    public LocalDate getWorkExamsExpiry() {
        return workExamsExpiry;
    }

    public String getDocumentNotes() {
        return documentNotes;
    }

    public void update(
            String fullName,
            String badgeCode,
            String cnhCategory,
            LocalDate cnhExpiry,
            AgentStatus status,
            String photoUrl,
            LocalDate medicalExamExpiry,
            LocalDate workExamsExpiry,
            String documentNotes
    ) {
        this.fullName = fullName;
        this.badgeCode = badgeCode;
        this.cnhCategory = cnhCategory;
        this.cnhExpiry = cnhExpiry;
        this.status = status;
        this.photoUrl = photoUrl;
        this.medicalExamExpiry = medicalExamExpiry;
        this.workExamsExpiry = workExamsExpiry;
        this.documentNotes = documentNotes;
    }
}
