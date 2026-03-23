package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
}
