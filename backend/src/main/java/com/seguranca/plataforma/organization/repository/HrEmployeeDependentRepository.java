package com.seguranca.plataforma.organization.repository;

import com.seguranca.plataforma.organization.model.HrEmployeeDependent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeDependentRepository extends JpaRepository<HrEmployeeDependent, Long> {
    List<HrEmployeeDependent> findAllByEmployeeIdOrderByIdAsc(Long employeeId);
}


