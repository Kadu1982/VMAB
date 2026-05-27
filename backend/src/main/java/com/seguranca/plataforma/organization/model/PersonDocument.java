package com.seguranca.plataforma.organization.model;

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
@Table(name = "person_documents")
public class PersonDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private PersonDocumentOwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private PersonDocumentType documentType;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, unique = true)
    private String storedFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(length = 1000)
    private String notes;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    protected PersonDocument() {
    }

    public PersonDocument(
            PersonDocumentOwnerType ownerType,
            Long ownerId,
            PersonDocumentType documentType,
            String originalFilename,
            String storedFilename,
            String contentType,
            long fileSizeBytes,
            String notes,
            String uploadedBy,
            OffsetDateTime uploadedAt
    ) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.documentType = documentType;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.notes = notes;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public PersonDocumentOwnerType getOwnerType() {
        return ownerType;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public PersonDocumentType getDocumentType() {
        return documentType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getNotes() {
        return notes;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }
}


