package com.seguranca.plataforma.operations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "shift_telemetry")
public class ShiftTelemetry {
    // Cada registro representa uma leitura de GPS persistida pelo app da ronda.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "speed_kmh", nullable = false)
    private double speedKmh;

    @Column(name = "accuracy_meters", nullable = false)
    private double accuracyMeters;

    @Column(name = "heading_degrees")
    private Double headingDegrees;

    @Column(name = "battery_level")
    private Double batteryLevel;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    protected ShiftTelemetry() {
    }

    public ShiftTelemetry(
            Long shiftId,
            Long agentId,
            Long vehicleId,
            double latitude,
            double longitude,
            double speedKmh,
            double accuracyMeters,
            Double headingDegrees,
            Double batteryLevel,
            OffsetDateTime recordedAt
    ) {
        this.shiftId = shiftId;
        this.agentId = agentId;
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.accuracyMeters = accuracyMeters;
        this.headingDegrees = headingDegrees;
        this.batteryLevel = batteryLevel;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getSpeedKmh() {
        return speedKmh;
    }

    public double getAccuracyMeters() {
        return accuracyMeters;
    }

    public Double getHeadingDegrees() {
        return headingDegrees;
    }

    public Double getBatteryLevel() {
        return batteryLevel;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

}


