package com.seguranca.plataforma.operations.dto;

import java.time.OffsetDateTime;
import java.util.List;

// Recorte operacional da patrulha em campo mostrado no web e no mobile.
public record ActivePatrolResponse(
        Long shiftId,
        Long agentId,
        String agentName,
        String agentBadgeCode,
        String agentPhotoUrl,
        String vehiclePlate,
        String vehicleModel,
        long vehicleCurrentKm,
        String vehicleStatus,
        double latitude,
        double longitude,
        double speedKmh,
        double accuracyMeters,
        double traveledKmInShift,
        int progressPercent,
        OffsetDateTime updatedAt,
        List<PatrolRouteStopResponse> routeStops,
        List<TelemetryTrailPointResponse> telemetryTrail
) {
}


