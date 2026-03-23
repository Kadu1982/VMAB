package com.seguranca.plataforma.auth;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // Retorna o usuario autenticado e os perfis ativos para o frontend ajustar a interface.

    @GetMapping("/me")
    public Map<String, Object> me(Principal principal, Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();
        return Map.of(
                "username", principal.getName(),
                "roles", roles
        );
    }
}
