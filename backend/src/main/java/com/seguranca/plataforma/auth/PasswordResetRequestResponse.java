package com.seguranca.plataforma.auth;

import java.time.OffsetDateTime;

public record PasswordResetRequestResponse(
        String username,
        String resetCode,
        OffsetDateTime expiresAt
) {
    // Resposta usada por suporte ou automacao para ler o codigo gerado no backend.
}
