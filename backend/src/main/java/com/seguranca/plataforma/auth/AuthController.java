package com.seguranca.plataforma.auth;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // Exponibiliza login por token e o perfil autenticado para web e mobile.

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            return authService.login(request);
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login invalido.");
        }
    }

    @PostMapping("/logout")
    public SessionActionResponse logout(Authentication authentication) {
        // Derruba o ciclo de sessao do usuario atual ao invalidar a versao dos tokens.
        return authService.logout(authentication.getName());
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.OK)
    public PasswordResetRequestResponse requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        // Gera um codigo temporario de recuperacao para o usuario informado.
        return authService.requestPasswordReset(request.username());
    }

    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.OK)
    public SessionActionResponse confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        // Conclui a troca de senha e invalida qualquer sessao emitida antes disso.
        return authService.confirmPasswordReset(request);
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();
        return authService.getAuthenticatedUser(authentication.getName(), roles);
    }
}
