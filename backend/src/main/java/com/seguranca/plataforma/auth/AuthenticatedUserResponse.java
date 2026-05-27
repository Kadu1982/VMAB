package com.seguranca.plataforma.auth;

import java.util.List;

public record AuthenticatedUserResponse(
        String username,
        List<String> roles,
        Long linkedAgentId,
        String linkedAgentName
) {
}


