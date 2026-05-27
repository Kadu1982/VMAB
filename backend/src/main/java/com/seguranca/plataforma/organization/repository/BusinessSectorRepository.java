package com.seguranca.plataforma.organization.repository;

import com.seguranca.plataforma.organization.model.BusinessSector;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessSectorRepository extends JpaRepository<BusinessSector, Long> {
    List<BusinessSector> findAllByBusinessUnitIdOrderByIdAsc(Long businessUnitId);
}


