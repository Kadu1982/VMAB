package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.IncidentEvidence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEvidenceRepository extends JpaRepository<IncidentEvidence, Long> {

    List<IncidentEvidence> findAllByOrderByUploadedAtDesc();

    List<IncidentEvidence> findByIncidentIdOrderByUploadedAtDesc(Long incidentId);

    Optional<IncidentEvidence> findByIdAndIncidentId(Long id, Long incidentId);
}
