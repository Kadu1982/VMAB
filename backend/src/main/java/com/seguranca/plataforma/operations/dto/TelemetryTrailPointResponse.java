package com.seguranca.plataforma.operations.dto;

import java.time.OffsetDateTime;

// Ponto historico usado para desenhar a rota percorrida pela viatura.
public record TelemetryTrailPointResponse(
        double latitude,
        double longitude,
        double speedKmh,
        double accuracyMeters,
        OffsetDateTime recordedAt
) {
}
