package com.seguranca.plataforma.operations.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "residents")
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(name = "reference_note")
    private String referenceNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResidentStatus status;

    @Column(name = "access_pin_hash")
    @JsonIgnore
    private String accessPinHash;

    @Column(name = "coercion_pin_hash")
    @JsonIgnore
    private String coercionPinHash;

    protected Resident() {
    }

    public Resident(
            String fullName,
            String phoneNumber,
            String address,
            String referenceNote,
            ResidentStatus status,
            String accessPinHash,
            String coercionPinHash
    ) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.referenceNote = referenceNote;
        this.status = status;
        this.accessPinHash = accessPinHash;
        this.coercionPinHash = coercionPinHash;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getReferenceNote() {
        return referenceNote;
    }

    public ResidentStatus getStatus() {
        return status;
    }

    public boolean isAccessPinConfigured() {
        return accessPinHash != null && !accessPinHash.isBlank();
    }

    public boolean isCoercionPinConfigured() {
        return coercionPinHash != null && !coercionPinHash.isBlank();
    }

    @JsonIgnore
    public String getAccessPinHash() {
        return accessPinHash;
    }

    @JsonIgnore
    public String getCoercionPinHash() {
        return coercionPinHash;
    }

    public void update(
            String fullName,
            String phoneNumber,
            String address,
            String referenceNote,
            ResidentStatus status,
            String accessPinHash,
            String coercionPinHash
    ) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.referenceNote = referenceNote;
        this.status = status;
        this.accessPinHash = accessPinHash;
        this.coercionPinHash = coercionPinHash;
    }
}
