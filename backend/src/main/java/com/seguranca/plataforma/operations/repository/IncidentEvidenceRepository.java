package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.IncidentEvidence;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEvidenceRepository extends JpaRepository<IncidentEvidence, Long> {

    List<IncidentEvidence> findAllByDeletedAtIsNullOrderByUploadedAtDesc();

    List<IncidentEvidence> findByIncidentIdAndDeletedAtIsNullOrderByUploadedAtDesc(Long incidentId);

    List<IncidentEvidence> findByUploadedAtBeforeAndDeletedAtIsNullOrderByUploadedAtAsc(OffsetDateTime uploadedAt);

    Optional<IncidentEvidence> findByIdAndIncidentId(Long id, Long incidentId);

    Optional<IncidentEvidence> findByIdAndIncidentIdAndDeletedAtIsNull(Long id, Long incidentId);
}


