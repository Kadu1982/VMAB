package com.seguranca.plataforma.auth;

import java.time.OffsetDateTime;

public record AppUserResponse(
        Long id,
        String username,
        AppUserRole role,
        boolean enabled,
        OffsetDateTime createdAt
) {
    public static AppUserResponse fromEntity(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
