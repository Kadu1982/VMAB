package com.seguranca.plataforma.organization.repository;

import com.seguranca.plataforma.organization.model.ResidentDependent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentDependentRepository extends JpaRepository<ResidentDependent, Long> {
    List<ResidentDependent> findAllByResidentIdOrderByIdAsc(Long residentId);
}
