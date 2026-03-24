package com.seguranca.plataforma.privacy.dto;

import com.seguranca.plataforma.privacy.model.PrivacyRequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePrivacyRequestRequest(
        @NotNull PrivacyRequestStatus status,
        String notes,
        boolean notifySubject,
        String notificationChannel,
        String notificationNotes
) {
}
