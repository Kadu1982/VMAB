package com.seguranca.plataforma.operations.model;

public record Vehicle(
        Long id,
        String plate,
        String model,
        long currentKm,
        long nextMaintenanceKm,
        VehicleStatus status
) {
}
