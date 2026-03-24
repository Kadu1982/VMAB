package com.seguranca.plataforma.privacy.dto;

import com.seguranca.plataforma.privacy.model.PrivacyRequestType;
import com.seguranca.plataforma.privacy.model.PrivacySubjectType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePrivacyRequestRequest(
        @NotNull PrivacyRequestType requestType,
        @NotNull PrivacySubjectType subjectType,
        @NotNull @Positive Long subjectId,
        String notes
) {
}
