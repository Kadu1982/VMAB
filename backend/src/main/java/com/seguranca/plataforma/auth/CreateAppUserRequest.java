package com.seguranca.plataforma.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppUserRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 6) String password,
        @NotNull AppUserRole role,
        boolean enabled
) {
}
