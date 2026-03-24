package com.seguranca.plataforma.privacy.repository;

import com.seguranca.plataforma.privacy.model.PrivacyRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivacyRequestRepository extends JpaRepository<PrivacyRequest, Long> {
    List<PrivacyRequest> findAllByOrderByRequestedAtDesc();
}
