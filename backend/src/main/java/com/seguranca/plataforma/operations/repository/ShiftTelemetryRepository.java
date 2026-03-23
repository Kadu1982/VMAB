package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.ShiftTelemetry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftTelemetryRepository extends JpaRepository<ShiftTelemetry, Long> {
    Optional<ShiftTelemetry> findTopByShiftIdOrderByRecordedAtDesc(Long shiftId);
    List<ShiftTelemetry> findByShiftIdOrderByRecordedAtAsc(Long shiftId);
}
