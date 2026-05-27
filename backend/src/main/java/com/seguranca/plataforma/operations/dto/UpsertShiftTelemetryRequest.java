package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record UpsertShiftTelemetryRequest(
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @NotNull @DecimalMin("0.0") Double speedKmh,
        @NotNull @DecimalMin("0.0") Double accuracyMeters,
        @DecimalMin("0.0") @DecimalMax("360.0") Double headingDegrees,
        @DecimalMin("0.0") @DecimalMax("1.0") Double batteryLevel,
        OffsetDateTime recordedAt
) {
}


