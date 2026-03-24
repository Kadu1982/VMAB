package com.seguranca.plataforma.operations.residentapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterResidentPushTokenRequest(
        @NotBlank @Size(max = 255) String expoPushToken,
        @Size(max = 120) String deviceLabel
) {
}
