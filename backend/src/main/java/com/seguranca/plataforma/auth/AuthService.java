package com.seguranca.plataforma.auth;

import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final long RESET_TOKEN_LENGTH_BYTES = 16L;
    private static final int RESET_TOKEN_EXPIRATION_MINUTES = 20;
    private static final int LOGIN_LOCK_MINUTES = 15;

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditRecordRepository auditRecordRepository;
    private final AgentRepository agentRepository;
    private final AppUserPushNotificationService appUserPushNotificationService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AuthenticationManager authenticationManager,
            AppUserRepository appUserRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AuditRecordRepository auditRecordRepository,
            AgentRepository agentRepository,
            AppUserPushNotificationService appUserPushNotificationService,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.auditRecordRepository = auditRecordRepository;
        this.agentRepository = agentRepository;
        this.appUserPushNotificationService = appUserPushNotificationService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        String username = request.username().trim();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Login invalido."));

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password())
            );
        } catch (BadCredentialsException exception) {
            registerFailedLoginAttempt(user);
            throw exception;
        } catch (LockedException exception) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Usuario bloqueado temporariamente.");
        } catch (DisabledException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario desativado.");
        }

        user.resetSecurityState();
        user.bumpTokenVersion();
        appUserRepository.save(user);
        recordAudit(AuditActionType.AUTH, "Auth", user.getId(), "Login realizado para o usuario " + user.getUsername());

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String token = jwtTokenService.generateToken(authentication.getName(), roles, user.getTokenVersion());
        OffsetDateTime expiresAt = jwtTokenService.extractExpiration(token);

        return new LoginResponse(token, "Bearer", expiresAt, authentication.getName(), roles, user.getTokenVersion());
    }

    @Transactional
    public SessionActionResponse logout(String username) {
        // Logout em JWT nao derruba um token especifico; invalida a versao de todos os tokens daquele usuario.
        AppUser user = getActiveUser(username.trim());
        user.bumpTokenVersion();
        appUserRepository.save(user);
        recordAudit(AuditActionType.AUTH, "Auth", user.getId(), "Logout realizado para o usuario " + user.getUsername());
        return new SessionActionResponse("Sessao encerrada com sucesso.");
    }

    @Transactional
    public SessionActionResponse registerPushDevice(String username, RegisterAppPushTokenRequest request) {
        // Registra o dispositivo Expo do usuario interno para notificacoes operacionais fora do painel web.
        AppUser user = getActiveUser(username.trim());
        appUserPushNotificationService.registerDevice(user, request.expoPushToken(), request.deviceLabel());
        recordAudit(AuditActionType.AUTH, "AuthPushDevice", user.getId(), "Dispositivo push registrado para o usuario " + user.getUsername());
        return new SessionActionResponse("Dispositivo operacional registrado com sucesso.");
    }

    @Transactional
    public SessionActionResponse revokePushDevice(String username, RevokeAppPushTokenRequest request) {
        // Revoga um token especifico para evitar push em aparelho que saiu de operacao.
        AppUser user = getActiveUser(username.trim());
        appUserPushNotificationService.revokeDevice(user, request.expoPushToken());
        recordAudit(AuditActionType.AUTH, "AuthPushDevice", user.getId(), "Dispositivo push revogado para o usuario " + user.getUsername());
        return new SessionActionResponse("Dispositivo operacional revogado com sucesso.");
    }

    @Transactional
    public PasswordResetRequestResponse requestPasswordReset(String username) {
        AppUser user = appUserRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));

        String resetCode = generateResetCode();
        String tokenHash = hashResetCode(resetCode);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plusMinutes(RESET_TOKEN_EXPIRATION_MINUTES);

        passwordResetTokenRepository.consumeActiveTokensByUserId(user.getId(), now);
        passwordResetTokenRepository.save(new PasswordResetToken(user.getId(), user.getUsername(), tokenHash, now, expiresAt));
        recordAudit(AuditActionType.AUTH, "Auth", user.getId(), "Solicitacao de reset de senha para o usuario " + user.getUsername());

        // O codigo retorna apenas para suporte/ambiente controlado; em producao isso deve ir por canal seguro.
        return new PasswordResetRequestResponse(user.getUsername(), resetCode, expiresAt);
    }

    @Transactional
    public SessionActionResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));

        String tokenHash = hashResetCode(request.resetCode().trim());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByUserIdAndTokenHashAndConsumedAtIsNullAndExpiresAtAfter(user.getId(), tokenHash, now)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo de reset invalido ou expirado."));

        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        user.resetSecurityState();
        user.bumpTokenVersion();
        appUserRepository.save(user);

        resetToken.consume(now);
        passwordResetTokenRepository.save(resetToken);
        recordAudit(AuditActionType.AUTH, "Auth", user.getId(), "Senha redefinida para o usuario " + user.getUsername());

        return new SessionActionResponse("Senha redefinida com sucesso.");
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse getAuthenticatedUser(String username, List<String> roles) {
        // Retorna o contexto autenticado com o eventual vinculo operacional da conta.
        AppUser user = appUserRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
        String linkedAgentName = user.getLinkedAgentId() == null
                ? null
                : agentRepository.findById(user.getLinkedAgentId()).map(agent -> agent.getFullName()).orElse(null);
        return new AuthenticatedUserResponse(user.getUsername(), roles, user.getLinkedAgentId(), linkedAgentName);
    }

    private void registerFailedLoginAttempt(AppUser user) {
        user.incrementFailedLoginAttempts();
        if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.lockUntil(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(LOGIN_LOCK_MINUTES));
            user.resetFailedLoginAttempts();
        }
        appUserRepository.save(user);
    }

    private AppUser getActiveUser(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario desativado.");
        }
        return user;
    }

    private String generateResetCode() {
        byte[] buffer = new byte[(int) RESET_TOKEN_LENGTH_BYTES];
        secureRandom.nextBytes(buffer);
        return HexFormat.of().formatHex(buffer).toUpperCase();
    }

    private String hashResetCode(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(code.trim().toUpperCase().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao processar codigo de reset.");
        }
    }

    private void recordAudit(AuditActionType actionType, String entityName, Long entityId, String description) {
        // Centraliza a trilha de eventos de autenticacao e ciclo de sessao.
        AuditRecord record = new AuditRecord(
                actionType,
                entityName,
                entityId,
                "auth-service",
                OffsetDateTime.now(ZoneOffset.UTC),
                description
        );
        auditRecordRepository.save(record);
    }
}
