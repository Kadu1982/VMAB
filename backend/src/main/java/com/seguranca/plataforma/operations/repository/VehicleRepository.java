package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}


