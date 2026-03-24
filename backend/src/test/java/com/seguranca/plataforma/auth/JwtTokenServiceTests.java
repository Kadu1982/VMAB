package com.seguranca.plataforma.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTests {

    @Test
    void deveGerarTokenComUsuarioPerfisEVersao() {
        // Garante o contrato minimo do token usado por web e mobile.
        JwtTokenService jwtTokenService = new JwtTokenService(new JwtProperties("segredo-de-teste-bem-grande-1234567890", 6));

        String token = jwtTokenService.generateToken("admin", List.of("ROLE_ADMIN"), 3);

        assertEquals("admin", jwtTokenService.extractUsername(token));
        assertEquals(List.of("ROLE_ADMIN"), jwtTokenService.extractRoles(token));
        assertEquals(3, jwtTokenService.extractTokenVersion(token));
        assertTrue(jwtTokenService.isValid(token, "admin"));
        assertFalse(jwtTokenService.isValid(token, "supervisor"));
    }
}
