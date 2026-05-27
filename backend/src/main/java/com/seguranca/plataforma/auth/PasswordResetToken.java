package com.seguranca.plataforma.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    // Guarda um codigo temporario de reset de senha com uso unico e validade curta.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    protected PasswordResetToken() {
    }

    public PasswordResetToken(Long userId, String username, String tokenHash, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        this.userId = userId;
        this.username = username;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
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

    public OffsetDateTime getConsumedAt() {
        return consumedAt;
    }

    public boolean isActive() {
        return consumedAt == null && expiresAt.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public void consume(OffsetDateTime consumedAt) {
        // Mantem o token de reset de senha unico e de uso unico.
        this.consumedAt = consumedAt;
    }
}


