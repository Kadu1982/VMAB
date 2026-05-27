package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentRepository extends JpaRepository<Resident, Long> {
}


