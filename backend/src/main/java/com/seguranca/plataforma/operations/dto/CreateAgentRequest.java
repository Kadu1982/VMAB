package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateAgentRequest(
        @NotBlank String fullName,
        @NotBlank String badgeCode,
        @NotBlank String cnhCategory,
        @NotNull LocalDate cnhExpiry,
        LocalDate birthDate,
        String photoUrl,
        String documentNotes
) {
}
