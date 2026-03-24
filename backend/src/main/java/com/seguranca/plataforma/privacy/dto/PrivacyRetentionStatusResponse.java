package com.seguranca.plataforma.privacy.dto;

import java.time.OffsetDateTime;

public record PrivacyRetentionStatusResponse(
        boolean enabled,
        String cleanupCron,
        int passwordResetTokenRetentionHours,
        int residentSessionRetentionDays,
        int incidentEvidenceRetentionDays,
        boolean removeOrphanEvidenceFiles,
        long openRequests,
        long inProgressRequests,
        long completedRequests,
        OffsetDateTime lastCleanupAt,
        String lastCleanupDescription
) {
}
