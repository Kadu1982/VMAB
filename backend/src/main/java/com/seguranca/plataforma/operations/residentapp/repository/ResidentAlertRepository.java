package com.seguranca.plataforma.operations.residentapp.repository;

import com.seguranca.plataforma.operations.residentapp.model.ResidentAlert;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentAlertRepository extends JpaRepository<ResidentAlert, Long> {
    List<ResidentAlert> findByResidentIdOrderByOpenedAtDesc(Long residentId);

    Optional<ResidentAlert> findByIdAndResidentId(Long id, Long residentId);

    Optional<ResidentAlert> findFirstByResidentIdAndStatusInOrderByOpenedAtDesc(Long residentId, List<ResidentAlertStatus> statuses);

    long countByStatus(ResidentAlertStatus status);
}


