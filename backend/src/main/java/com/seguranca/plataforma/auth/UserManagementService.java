package com.seguranca.plataforma.auth;

import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserManagementService {
    private final AppUserRepository appUserRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(AppUserRepository appUserRepository, AgentRepository agentRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.agentRepository = agentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUserResponse> listUsers() {
        List<AppUser> users = appUserRepository.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getId))
                .toList();
        Map<Long, String> agentNames = loadAgentNames(users.stream().map(AppUser::getLinkedAgentId).toList());
        return users.stream()
                // Usuario sem agente vinculado e valido; o mapa imutavel do Java não aceita get(null).
                .map(user -> AppUserResponse.fromEntity(
                        user,
                        user.getLinkedAgentId() != null ? agentNames.get(user.getLinkedAgentId()) : null
                ))
                .toList();
    }

    @Transactional
    public AppUserResponse createUser(CreateAppUserRequest request) {
        String username = request.username().trim();
        appUserRepository.findByUsername(username)
                .ifPresent(user -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um usuário com esse login.");
                });

        AppUser user = new AppUser(
                username,
                passwordEncoder.encode(request.password()),
                request.role(),
                request.enabled(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        user.update(username, request.role(), request.enabled(), resolveLinkedAgentId(request.role(), request.linkedAgentId()));

        AppUser savedUser = appUserRepository.save(user);
        return AppUserResponse.fromEntity(savedUser, resolveLinkedAgentName(savedUser.getLinkedAgentId()));
    }

    @Transactional
    public AppUserResponse updateUser(Long id, UpdateAppUserRequest request) {
        AppUser user = getUser(id);
        boolean enabledChanged = user.isEnabled() != request.enabled();

        appUserRepository.findByUsername(request.username().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um usuário com esse login.");
                });

        user.update(
                request.username().trim(),
                request.role(),
                request.enabled(),
                resolveLinkedAgentId(request.role(), request.linkedAgentId())
        );
        if (StringUtils.hasText(request.password())) {
            user.updatePasswordHash(passwordEncoder.encode(request.password()));
            user.resetSecurityState();
        }

        if (enabledChanged && request.enabled()) {
            // Reabilitar um usuário tambem limpa bloqueios temporarios anteriores.
            user.resetSecurityState();
        }

        // Qualquer alteracao administrativa invalida tokens emitidos antes desta mudanca.
        user.bumpTokenVersion();

        AppUser savedUser = appUserRepository.save(user);
        return AppUserResponse.fromEntity(savedUser, resolveLinkedAgentName(savedUser.getLinkedAgentId()));
    }

    @Transactional
    public void deleteUser(Long id) {
        AppUser user = getUser(id);
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O usuário admin padrao não pode ser removido.");
        }

        appUserRepository.delete(user);
    }

    private AppUser getUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não encontrado."));
    }

    private Long resolveLinkedAgentId(AppUserRole role, Long linkedAgentId) {
        // O vínculo opcional com agente permite amarrar a identidade da ronda a um vigilante real.
        if (linkedAgentId == null) {
            return null;
        }

        if (role != AppUserRole.RONDA && role != AppUserRole.SUPERVISOR && role != AppUserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Somente usuários operacionais podem ser vinculados a um vigilante.");
        }

        agentRepository.findById(linkedAgentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente vinculado não encontrado."));
        return linkedAgentId;
    }

    private Map<Long, String> loadAgentNames(Collection<Long> linkedAgentIds) {
        List<Long> validIds = linkedAgentIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (validIds.isEmpty()) {
            return Map.of();
        }

        return agentRepository.findAllById(validIds).stream()
                .collect(Collectors.toMap(Agent::getId, Agent::getFullName, (left, right) -> left));
    }

    private String resolveLinkedAgentName(Long linkedAgentId) {
        if (linkedAgentId == null) {
            return null;
        }
        return agentRepository.findById(linkedAgentId)
                .map(Agent::getFullName)
                .orElse(null);
    }
}


