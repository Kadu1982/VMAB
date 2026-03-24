package com.seguranca.plataforma.operations.residentapp.dto;

import jakarta.validation.constraints.Size;

public record ResidentAlertCancelRequest(
        @Size(max = 500) String cancellationReason
) {
}
