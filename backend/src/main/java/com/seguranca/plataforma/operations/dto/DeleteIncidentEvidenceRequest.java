package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.Size;

public record DeleteIncidentEvidenceRequest(
        @Size(max = 1000) String reason
) {
}


