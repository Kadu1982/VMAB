package com.seguranca.plataforma.organization.repository;

import com.seguranca.plataforma.organization.model.HrEmployeeAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeAssignmentRepository extends JpaRepository<HrEmployeeAssignment, Long> {
    List<HrEmployeeAssignment> findAllByEmployeeIdOrderByIdAsc(Long employeeId);
}
