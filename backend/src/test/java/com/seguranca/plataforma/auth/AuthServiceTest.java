package com.seguranca.plataforma.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private AuditRecordRepository auditRecordRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AppUserPushNotificationService appUserPushNotificationService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginDeveGerarTokenEResetarEstadoDeSegurança() {
        // Valida o contrato principal do login: autenticar, limpar estado antigo e emitir novo JWT.
        AppUser user = new AppUser(
                "admin",
                "{noop}senha",
                AppUserRole.ADMIN,
                true,
                OffsetDateTime.parse("2026-03-23T10:00:00Z")
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                "senha",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtTokenService.generateToken("admin", List.of("ROLE_ADMIN"), 1)).thenReturn("token-1");
        when(jwtTokenService.extractExpiration("token-1")).thenReturn(OffsetDateTime.parse("2026-03-23T16:00:00Z"));
        when(appUserRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = authService.login(new LoginRequest(" admin ", "senha"));

        assertEquals("token-1", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("admin", response.username());
        assertEquals(List.of("ROLE_ADMIN"), response.roles());
        assertEquals(1, response.tokenVersion());
        assertEquals(1, user.getTokenVersion());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        verify(auditRecordRepository).save(any(AuditRecord.class));
    }

    @Test
    void loginFalhaCincoVezesDeveBloquearUsuario() {
        // O sistema precisa travar a conta depois da janela de tentativas erradas, sem deixar o estado solto.
        AppUser user = new AppUser(
                "admin",
                "{noop}senha",
                AppUserRole.ADMIN,
                true,
                OffsetDateTime.parse("2026-03-23T10:00:00Z")
        );
        user.incrementFailedLoginAttempts();
        user.incrementFailedLoginAttempts();
        user.incrementFailedLoginAttempts();
        user.incrementFailedLoginAttempts();

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Login invalido."));
        when(appUserRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(BadCredentialsException.class, () -> authService.login(new LoginRequest("admin", "senha")));

        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.isLocked());
        verify(appUserRepository).save(user);
        verifyNoMoreInteractions(auditRecordRepository);
    }

    @Test
    void requestPasswordResetDeveGerarCodigoEGravarToken() {
        // Mantem o fluxo de recuperacao rastreavel sem expor o hash no teste.
        AppUser user = new AppUser(
                "supervisor",
                "{noop}senha",
                AppUserRole.SUPERVISOR,
                true,
                OffsetDateTime.parse("2026-03-23T10:00:00Z")
        );

        when(appUserRepository.findByUsername("supervisor")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.consumeActiveTokensByUserId(isNull(), any())).thenReturn(1);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetRequestResponse response = authService.requestPasswordReset(" supervisor ");

        assertEquals("supervisor", response.username());
        assertNotNull(response.resetCode());
        assertEquals(32, response.resetCode().length());
        assertTrue(response.resetCode().matches("[A-F0-9]{32}"));
        assertTrue(response.expiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC)));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertTrue(tokenCaptor.getValue().getTokenHash().matches("[0-9a-f]{64}"));
        verify(auditRecordRepository).save(any(AuditRecord.class));
    }

    @Test
    void confirmPasswordResetDeveAtualizarSenhaEConsumirToken() {
        // Fecha a recuperacao de senha com troca de hash e invalida qualquer codigo anterior.
        AppUser user = new AppUser(
                "cliente",
                "{noop}antiga",
                AppUserRole.CLIENT,
                true,
                OffsetDateTime.parse("2026-03-23T10:00:00Z")
        );

        PasswordResetToken token = new PasswordResetToken(
                null,
                "cliente",
                "hash-irrelevante-no-mock",
                OffsetDateTime.parse("2026-03-23T10:00:00Z"),
                OffsetDateTime.parse("2026-03-23T11:00:00Z")
        );

        when(appUserRepository.findByUsername("cliente")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findByUserIdAndTokenHashAndConsumedAtIsNullAndExpiresAtAfter(isNull(), anyString(), any()))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("nova-senha")).thenReturn("{bcrypt}nova-senha");
        when(appUserRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordResetTokenRepository.save(token)).thenAnswer(invocation -> invocation.getArgument(0));

        SessionActionResponse response = authService.confirmPasswordReset(
                new PasswordResetConfirmRequest(" cliente ", "codigo-qualquer", "nova-senha")
        );

        assertEquals("Senha redefinida com sucesso.", response.message());
        assertEquals("{bcrypt}nova-senha", user.getPasswordHash());
        assertEquals(1, user.getTokenVersion());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        assertNotNull(token.getConsumedAt());
        verify(auditRecordRepository).save(any(AuditRecord.class));
    }

    @Test
    void registerPushDeviceDeveDelegarAoServicoDePush() {
        AppUser user = new AppUser(
                "ronda",
                "{noop}senha",
                AppUserRole.RONDA,
                true,
                OffsetDateTime.parse("2026-03-23T10:00:00Z")
        );
        when(appUserRepository.findByUsername("ronda")).thenReturn(Optional.of(user));

        SessionActionResponse response = authService.registerPushDevice(
                "ronda",
                new RegisterAppPushTokenRequest("ExponentPushToken[abc]", "android-ronda")
        );

        assertEquals("Dispositivo operacional registrado com sucesso.", response.message());
        verify(appUserPushNotificationService).registerDevice(user, "ExponentPushToken[abc]", "android-ronda");
        verify(auditRecordRepository).save(any(AuditRecord.class));
    }

    @Test
    void revokePushDeviceDeveDelegarAoServicoDePush() {
        AppUser user = new AppUser(
                "ronda",
                "{noop}senha",
                AppUserRole.RONDA,
                true,
                OffsetDateTime.parse("2026-03-23T10:00:00Z")
        );
        when(appUserRepository.findByUsername("ronda")).thenReturn(Optional.of(user));

        SessionActionResponse response = authService.revokePushDevice(
                "ronda",
                new RevokeAppPushTokenRequest("ExponentPushToken[abc]")
        );

        assertEquals("Dispositivo operacional revogado com sucesso.", response.message());
        verify(appUserPushNotificationService).revokeDevice(user, "ExponentPushToken[abc]");
        verify(auditRecordRepository).save(any(AuditRecord.class));
    }
}


