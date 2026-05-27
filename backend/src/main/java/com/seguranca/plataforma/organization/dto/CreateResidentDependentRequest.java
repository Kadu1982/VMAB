package com.seguranca.plataforma.organization.dto;

import com.seguranca.plataforma.organization.model.FamilyRelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateResidentDependentRequest(
        @NotBlank String fullName,
        String cpf,
        String phoneNumber,
        @NotNull FamilyRelationshipType relationship,
        boolean accessEnabled,
        boolean appEnabled,
        String notes
) {
}


