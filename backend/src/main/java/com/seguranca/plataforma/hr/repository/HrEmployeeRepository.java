package com.seguranca.plataforma.hr.repository;

import com.seguranca.plataforma.hr.model.HrEmployee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeRepository extends JpaRepository<HrEmployee, Long> {
    Optional<HrEmployee> findByLinkedAgentId(Long linkedAgentId);

    Optional<HrEmployee> findByEmployeeCodeIgnoreCase(String employeeCode);

    List<HrEmployee> findAllByOrderByIdAsc();
}
