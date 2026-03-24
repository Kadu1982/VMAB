package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleMaintenanceRecordRepository extends JpaRepository<VehicleMaintenanceRecord, Long> {
    List<VehicleMaintenanceRecord> findByVehicleIdOrderByOpenedAtDesc(Long vehicleId);
}
