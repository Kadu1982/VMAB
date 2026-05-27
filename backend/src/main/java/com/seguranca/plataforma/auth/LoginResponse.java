package com.seguranca.plataforma.auth;

import java.time.OffsetDateTime;
import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt,
        String username,
        List<String> roles,
        int tokenVersion
) {
}


