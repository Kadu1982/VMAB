package com.seguranca.plataforma.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAppUserRequest(
        @NotBlank String username,
        @NotNull AppUserRole role,
        boolean enabled,
        @Size(min = 6) String password
) {
}
