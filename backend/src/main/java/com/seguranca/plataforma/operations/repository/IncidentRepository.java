package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
}
