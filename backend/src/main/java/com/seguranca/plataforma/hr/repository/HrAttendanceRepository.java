package com.seguranca.plataforma.hr.repository;

import com.seguranca.plataforma.hr.model.HrAttendance;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrAttendanceRepository extends JpaRepository<HrAttendance, Long> {
    List<HrAttendance> findAllByOrderByOccurredAtDesc();

    List<HrAttendance> findTop20ByOrderByOccurredAtDesc();

    Optional<HrAttendance> findTopByEmployeeIdOrderByOccurredAtDesc(Long employeeId);

    List<HrAttendance> findByOccurredAtBeforeOrderByOccurredAtAsc(OffsetDateTime cutoff);
}


