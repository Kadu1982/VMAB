package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleMaintenanceRecordRepository extends JpaRepository<VehicleMaintenanceRecord, Long> {
    List<VehicleMaintenanceRecord> findByVehicleIdOrderByOpenedAtDesc(Long vehicleId);

    @Query("""
            select record
            from VehicleMaintenanceRecord record
            where record.resolved = true
              and (
                    (record.completedAt is not null and record.completedAt < :cutoff)
                 or (record.completedAt is null and record.openedAt < :cutoff)
              )
            order by coalesce(record.completedAt, record.openedAt) asc
            """)
    List<VehicleMaintenanceRecord> findRetainableRecordsBefore(@Param("cutoff") OffsetDateTime cutoff);
}


