package com.seguranca.plataforma.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank String username,
        @NotBlank String resetCode,
        @NotBlank @Size(min = 6) String newPassword
) {
    // DTO que conclui a troca de senha com o codigo temporario gerado no suporte.
}


