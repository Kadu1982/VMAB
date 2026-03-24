package com.seguranca.plataforma.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterAppPushTokenRequest(
        @NotBlank String expoPushToken,
        String deviceLabel
) {
}
