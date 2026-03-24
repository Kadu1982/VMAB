package com.seguranca.plataforma.auth;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank String username
) {
    // DTO minimo para solicitar um codigo temporario de recuperacao de senha.
}
