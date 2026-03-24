package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.NotBlank;

public record RondaCloseIncidentRequest(
        @NotBlank String closureNotes
) {
}
