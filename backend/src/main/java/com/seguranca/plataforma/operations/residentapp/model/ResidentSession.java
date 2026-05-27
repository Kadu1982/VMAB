package com.seguranca.plataforma.operations.residentapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "resident_sessions")
public class ResidentSession {
    // Token separado do login administrativo para não misturar os dois fluxos.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    protected ResidentSession() {
    }

    public ResidentSession(Long residentId, String tokenHash, OffsetDateTime createdAt, OffsetDateTime expiresAt, OffsetDateTime lastSeenAt) {
        this.residentId = residentId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.lastSeenAt = lastSeenAt;
    }

    public Long getId() {
        return id;
    }

    public Long getResidentId() {
        return residentId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public boolean isActive(OffsetDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void touch(OffsetDateTime now) {
        this.lastSeenAt = now;
    }

    public void revoke(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}


