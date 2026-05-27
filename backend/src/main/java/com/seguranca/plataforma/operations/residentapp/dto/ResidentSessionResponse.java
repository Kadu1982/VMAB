package com.seguranca.plataforma.operations.residentapp.dto;

import java.time.OffsetDateTime;

public record ResidentSessionResponse(
        String tokenType,
        String accessToken,
        OffsetDateTime expiresAt,
        Long residentId,
        String fullName,
        String phoneNumber,
        String address,
        String referenceNote
) {
}


