package com.seguranca.plataforma.operations.residentapp.dto;

import jakarta.validation.constraints.Size;

public record ResidentAlertActionNotesRequest(
        @Size(max = 1000) String notes
) {
}


