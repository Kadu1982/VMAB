package com.seguranca.plataforma.organization.repository;

import com.seguranca.plataforma.organization.model.BusinessUnit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, Long> {
    List<BusinessUnit> findAllByOrderByIdAsc();
}


