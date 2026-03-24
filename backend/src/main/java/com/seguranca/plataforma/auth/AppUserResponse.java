package com.seguranca.plataforma.auth;

import java.time.OffsetDateTime;

public record AppUserResponse(
        Long id,
        String username,
        AppUserRole role,
        boolean enabled,
        OffsetDateTime createdAt,
        int tokenVersion,
        int failedLoginAttempts,
        OffsetDateTime lockedUntil,
        Long linkedAgentId,
        String linkedAgentName
) {
    public static AppUserResponse fromEntity(AppUser user, String linkedAgentName) {
        return new AppUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getTokenVersion(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getLinkedAgentId(),
                linkedAgentName
        );
    }
}
