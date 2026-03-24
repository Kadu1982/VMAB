package com.seguranca.plataforma.auth;

import jakarta.validation.constraints.NotBlank;

public record RevokeAppPushTokenRequest(
        @NotBlank String expoPushToken
) {
}
