package com.seguranca.plataforma.privacy.dto;

import com.seguranca.plataforma.privacy.model.PrivacySubjectType;
import java.time.OffsetDateTime;
import java.util.Map;

public record PrivacyExportResponse(
        PrivacySubjectType subjectType,
        Long subjectId,
        String subjectLabel,
        OffsetDateTime exportedAt,
        Map<String, Object> payload
) {
}
