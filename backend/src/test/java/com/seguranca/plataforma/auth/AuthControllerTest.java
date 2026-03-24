package com.seguranca.plataforma.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginDeveRetornarJwtQuandoCredenciaisForemValidas() {
        // O controller so faz a ponte HTTP -> service; o contrato principal e devolver o payload do login.
        when(authService.login(any(LoginRequest.class))).thenReturn(new LoginResponse(
                "token-123",
                "Bearer",
                OffsetDateTime.parse("2026-03-23T16:00:00Z"),
                "admin",
                List.of("ROLE_ADMIN"),
                2
        ));

        LoginResponse response = authController.login(new LoginRequest("admin", "senha"));

        assertEquals("token-123", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("admin", response.username());
        assertEquals(List.of("ROLE_ADMIN"), response.roles());
        assertEquals(2, response.tokenVersion());
        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void loginDeveResponder401QuandoAsCredenciaisForemInvalidas() {
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Login invalido."));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.login(new LoginRequest("admin", "senha"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void logoutDeveInvalidarAversaoDaSessaoAtual() {
        // O logout invalida a versao dos tokens do usuario atual, nao um JWT isolado.
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        when(authService.logout("admin")).thenReturn(new SessionActionResponse("Sessao encerrada com sucesso."));

        SessionActionResponse response = authController.logout(authentication);

        assertEquals("Sessao encerrada com sucesso.", response.message());
        verify(authService).logout(eq("admin"));
    }

    @Test
    void requestPasswordResetDeveEntregarCodigoTemporario() {
        when(authService.requestPasswordReset("admin")).thenReturn(new PasswordResetRequestResponse(
                "admin",
                "ABCDEF1234567890ABCDEF1234567890",
                OffsetDateTime.parse("2026-03-23T12:00:00Z")
        ));

        PasswordResetRequestResponse response = authController.requestPasswordReset(new PasswordResetRequest("admin"));

        assertEquals("admin", response.username());
        assertEquals("ABCDEF1234567890ABCDEF1234567890", response.resetCode());
    }

    @Test
    void confirmPasswordResetDeveFinalizarARecuperacao() {
        when(authService.confirmPasswordReset(any(PasswordResetConfirmRequest.class)))
                .thenReturn(new SessionActionResponse("Senha redefinida com sucesso."));

        SessionActionResponse response = authController.confirmPasswordReset(
                new PasswordResetConfirmRequest("admin", "ABCDEF1234567890ABCDEF1234567890", "nova-senha")
        );

        assertEquals("Senha redefinida com sucesso.", response.message());
    }

    @Test
    void meDeveExporUsuarioERolesDaSessao() {
        Authentication authentication = mock(Authentication.class);
        Principal principal = mock(Principal.class);
        List<org.springframework.security.core.GrantedAuthority> authorities = List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPERVISOR")
        );
        when(principal.getName()).thenReturn("admin");
        doReturn(authorities).when(authentication).getAuthorities();

        var response = authController.me(principal, authentication);

        assertEquals("admin", response.get("username"));
        assertEquals(List.of("ROLE_ADMIN", "ROLE_SUPERVISOR"), response.get("roles"));
    }
}
