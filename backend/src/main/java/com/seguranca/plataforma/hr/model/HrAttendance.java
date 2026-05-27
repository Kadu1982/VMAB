package com.seguranca.plataforma.hr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "hr_attendance_records")
public class HrAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private HrAttendanceType eventType;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "device_label")
    private String deviceLabel;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(length = 500)
    private String note;

    @Column(name = "anomaly_flag", nullable = false)
    private boolean anomalyFlag;

    @Column(name = "anomaly_reason")
    private String anomalyReason;

    protected HrAttendance() {
    }

    public HrAttendance(
            Long employeeId,
            HrAttendanceType eventType,
            OffsetDateTime occurredAt,
            String deviceLabel,
            Double latitude,
            Double longitude,
            String note,
            boolean anomalyFlag,
            String anomalyReason
    ) {
        this.employeeId = employeeId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.deviceLabel = deviceLabel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.anomalyFlag = anomalyFlag;
        this.anomalyReason = anomalyReason;
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public HrAttendanceType getEventType() {
        return eventType;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getNote() {
        return note;
    }

    public boolean isAnomalyFlag() {
        return anomalyFlag;
    }

    public String getAnomalyReason() {
        return anomalyReason;
    }
}


