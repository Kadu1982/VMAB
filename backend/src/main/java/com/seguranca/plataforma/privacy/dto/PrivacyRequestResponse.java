package com.seguranca.plataforma.privacy.dto;

import com.seguranca.plataforma.privacy.model.PrivacyRequest;
import com.seguranca.plataforma.privacy.model.PrivacyRequestStatus;
import com.seguranca.plataforma.privacy.model.PrivacyRequestType;
import com.seguranca.plataforma.privacy.model.PrivacySubjectType;
import java.time.OffsetDateTime;

public record PrivacyRequestResponse(
        Long id,
        PrivacyRequestType requestType,
        PrivacySubjectType subjectType,
        Long subjectId,
        String subjectLabel,
        PrivacyRequestStatus status,
        String requestedBy,
        OffsetDateTime requestedAt,
        String handledBy,
        OffsetDateTime handledAt,
        String notes,
        OffsetDateTime subjectNotifiedAt,
        String subjectNotificationChannel,
        String subjectNotificationNotes,
        OffsetDateTime exportGeneratedAt,
        OffsetDateTime deletionAppliedAt
) {
    public static PrivacyRequestResponse fromEntity(PrivacyRequest request) {
        return new PrivacyRequestResponse(
                request.getId(),
                request.getRequestType(),
                request.getSubjectType(),
                request.getSubjectId(),
                request.getSubjectLabel(),
                request.getStatus(),
                request.getRequestedBy(),
                request.getRequestedAt(),
                request.getHandledBy(),
                request.getHandledAt(),
                request.getNotes(),
                request.getSubjectNotifiedAt(),
                request.getSubjectNotificationChannel(),
                request.getSubjectNotificationNotes(),
                request.getExportGeneratedAt(),
                request.getDeletionAppliedAt()
        );
    }
}
