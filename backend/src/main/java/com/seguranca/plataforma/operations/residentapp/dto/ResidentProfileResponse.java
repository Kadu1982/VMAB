package com.seguranca.plataforma.operations.residentapp.dto;

import java.time.OffsetDateTime;

public record ResidentProfileResponse(
        Long residentId,
        String fullName,
        String phoneNumber,
        String address,
        String referenceNote,
        OffsetDateTime sessionExpiresAt
) {
}


