package com.seguranca.plataforma.operations.residentapp.repository;

import com.seguranca.plataforma.operations.residentapp.model.ResidentPushDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentPushDeviceRepository extends JpaRepository<ResidentPushDevice, Long> {
    List<ResidentPushDevice> findByResidentIdAndRevokedAtIsNullOrderByUpdatedAtDesc(Long residentId);

    Optional<ResidentPushDevice> findByResidentIdAndExpoPushToken(Long residentId, String expoPushToken);

    Optional<ResidentPushDevice> findByExpoPushToken(String expoPushToken);
}
