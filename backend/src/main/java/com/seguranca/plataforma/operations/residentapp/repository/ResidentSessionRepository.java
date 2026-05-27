package com.seguranca.plataforma.operations.residentapp.repository;

import com.seguranca.plataforma.operations.residentapp.model.ResidentSession;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentSessionRepository extends JpaRepository<ResidentSession, Long> {
    Optional<ResidentSession> findByTokenHash(String tokenHash);

    List<ResidentSession> findByResidentIdOrderByCreatedAtDesc(Long residentId);
    List<ResidentSession> findByExpiresAtBeforeOrRevokedAtBefore(OffsetDateTime expiresAt, OffsetDateTime revokedAt);
}


