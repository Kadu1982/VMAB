package com.seguranca.plataforma.organization.repository;

import com.seguranca.plataforma.organization.model.ResidentVehicle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentVehicleRepository extends JpaRepository<ResidentVehicle, Long> {
    List<ResidentVehicle> findAllByResidentIdOrderByIdAsc(Long residentId);
}


