package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.AgentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateAgentRequest(
        @NotBlank String fullName,
        @NotBlank String badgeCode,
        @NotBlank String cnhCategory,
        @NotNull LocalDate cnhExpiry,
        LocalDate birthDate,
        @NotNull AgentStatus status,
        String photoUrl,
        String documentNotes
) {
}


