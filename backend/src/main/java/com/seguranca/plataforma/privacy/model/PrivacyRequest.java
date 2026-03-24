package com.seguranca.plataforma.privacy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "privacy_requests")
public class PrivacyRequest {
    // Registra pedidos de exportacao ou exclusao para manter a trilha de LGPD auditavel.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private PrivacyRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false)
    private PrivacySubjectType subjectType;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "subject_label", nullable = false)
    private String subjectLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrivacyRequestStatus status;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "handled_by")
    private String handledBy;

    @Column(name = "handled_at")
    private OffsetDateTime handledAt;

    @Column(length = 1000)
    private String notes;

    protected PrivacyRequest() {
    }

    public PrivacyRequest(
            PrivacyRequestType requestType,
            PrivacySubjectType subjectType,
            Long subjectId,
            String subjectLabel,
            PrivacyRequestStatus status,
            String requestedBy,
            OffsetDateTime requestedAt,
            String notes
    ) {
        this.requestType = requestType;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.subjectLabel = subjectLabel;
        this.status = status;
        this.requestedBy = requestedBy;
        this.requestedAt = requestedAt;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public PrivacyRequestType getRequestType() {
        return requestType;
    }

    public PrivacySubjectType getSubjectType() {
        return subjectType;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectLabel() {
        return subjectLabel;
    }

    public PrivacyRequestStatus getStatus() {
        return status;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public OffsetDateTime getHandledAt() {
        return handledAt;
    }

    public String getNotes() {
        return notes;
    }

    public void updateStatus(PrivacyRequestStatus status, String handledBy, OffsetDateTime handledAt, String notes) {
        this.status = status;
        this.handledBy = handledBy;
        this.handledAt = handledAt;
        this.notes = notes;
    }
}
