package com.seguranca.plataforma.operations.residentapp.repository;

import com.seguranca.plataforma.operations.residentapp.model.ResidentSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentSessionRepository extends JpaRepository<ResidentSession, Long> {
    Optional<ResidentSession> findByTokenHash(String tokenHash);
}
