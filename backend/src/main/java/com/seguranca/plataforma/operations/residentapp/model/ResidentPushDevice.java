package com.seguranca.plataforma.operations.residentapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "resident_push_devices")
public class ResidentPushDevice {
    // Guarda os dispositivos Expo do morador para que o backend consiga avisar mudancas criticas do alerta.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "expo_push_token", nullable = false, unique = true, length = 255)
    private String expoPushToken;

    @Column(name = "device_label", length = 120)
    private String deviceLabel;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    protected ResidentPushDevice() {
    }

    public ResidentPushDevice(Long residentId, String expoPushToken, String deviceLabel, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.residentId = residentId;
        this.expoPushToken = expoPushToken;
        this.deviceLabel = deviceLabel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getResidentId() {
        return residentId;
    }

    public String getExpoPushToken() {
        return expoPushToken;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public boolean isActive() {
        return revokedAt == null;
    }

    public void refresh(Long residentId, String deviceLabel, OffsetDateTime updatedAt) {
        this.residentId = residentId;
        this.deviceLabel = deviceLabel;
        this.updatedAt = updatedAt;
        this.revokedAt = null;
    }

    public void revoke(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
        this.updatedAt = revokedAt;
    }
}
