package com.seguranca.plataforma.operations.dto;

import java.time.OffsetDateTime;

public record IncidentEvidenceResponse(
        Long id,
        Long incidentId,
        String incidentResidentName,
        String originalFilename,
        String contentType,
        long fileSizeBytes,
        String notes,
        String uploadedBy,
        OffsetDateTime uploadedAt,
        OffsetDateTime retentionExpiresAt,
        String downloadPath
) {
}


