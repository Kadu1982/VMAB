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

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "document_notes", length = 500)
    private String documentNotes;

    protected Agent() {
    }

    public Agent(
            String fullName,
            String badgeCode,
            String cnhCategory,
            LocalDate cnhExpiry,
            LocalDate birthDate,
            AgentStatus status,
            String photoUrl,
            String documentNotes
    ) {
        this.fullName = fullName;
        this.badgeCode = badgeCode;
        this.cnhCategory = cnhCategory;
        this.cnhExpiry = cnhExpiry;
        this.birthDate = birthDate;
        this.status = status;
        this.photoUrl = photoUrl;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getDocumentNotes() {
        return documentNotes;
    }

    public void update(
            String fullName,
            String badgeCode,
            String cnhCategory,
            LocalDate cnhExpiry,
            LocalDate birthDate,
            AgentStatus status,
            String photoUrl,
            String documentNotes
    ) {
        this.fullName = fullName;
        this.badgeCode = badgeCode;
        this.cnhCategory = cnhCategory;
        this.cnhExpiry = cnhExpiry;
        this.birthDate = birthDate;
        this.status = status;
        this.photoUrl = photoUrl;
        this.documentNotes = documentNotes;
    }

    public void anonymizePersonalData() {
        // A anonimizacao preserva o registro operacional, mas retira os dados pessoais diretos do vigilante.
        this.fullName = "Agente removido #" + id;
        this.photoUrl = null;
        this.birthDate = null;
        this.documentNotes = null;
        this.status = AgentStatus.BLOCKED;
    }
}


