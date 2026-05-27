package com.seguranca.plataforma.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "app_user_push_devices")
public class AppUserPushDevice {
    // Guarda os dispositivos Expo dos usuários internos para push operacional remoto.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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

    protected AppUserPushDevice() {
    }

    public AppUserPushDevice(Long userId, String expoPushToken, String deviceLabel, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.userId = userId;
        this.expoPushToken = expoPushToken;
        this.deviceLabel = deviceLabel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
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

    public void refresh(Long userId, String deviceLabel, OffsetDateTime updatedAt) {
        this.userId = userId;
        this.deviceLabel = deviceLabel;
        this.updatedAt = updatedAt;
        this.revokedAt = null;
    }

    public void revoke(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
        this.updatedAt = revokedAt;
    }
}


