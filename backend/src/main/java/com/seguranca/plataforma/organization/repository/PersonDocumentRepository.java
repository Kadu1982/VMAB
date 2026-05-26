package com.seguranca.plataforma.organization.repository;

import com.seguranca.plataforma.organization.model.PersonDocument;
import com.seguranca.plataforma.organization.model.PersonDocumentOwnerType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonDocumentRepository extends JpaRepository<PersonDocument, Long> {
    List<PersonDocument> findAllByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(PersonDocumentOwnerType ownerType, Long ownerId);

    Optional<PersonDocument> findByIdAndOwnerTypeAndOwnerId(Long id, PersonDocumentOwnerType ownerType, Long ownerId);
}
