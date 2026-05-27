package com.seguranca.plataforma.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppUserRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "linked_agent_id")
    private Long linkedAgentId;

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, AppUserRole role, boolean enabled, OffsetDateTime createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.tokenVersion = 0;
        this.failedLoginAttempts = 0;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AppUserRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public OffsetDateTime getLockedUntil() {
        return lockedUntil;
    }

    public Long getLinkedAgentId() {
        return linkedAgentId;
    }

    public boolean isLocked() {
        // Bloqueio temporario e respeitado apenas enquanto a janela estiver ativa.
        return lockedUntil != null && lockedUntil.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public void update(String username, AppUserRole role, boolean enabled, Long linkedAgentId) {
        this.username = username;
        this.role = role;
        this.enabled = enabled;
        this.linkedAgentId = linkedAgentId;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lockUntil(OffsetDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public void bumpTokenVersion() {
        // Cada alteracao sensivel invalida as sessoes emitidas antes desta versao.
        this.tokenVersion++;
    }

    public void resetSecurityState() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
}


