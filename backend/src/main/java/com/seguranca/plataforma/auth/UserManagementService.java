package com.seguranca.plataforma.auth;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserManagementService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUserResponse> listUsers() {
        return appUserRepository.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getId))
                .map(AppUserResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AppUserResponse createUser(CreateAppUserRequest request) {
        appUserRepository.findByUsername(request.username())
                .ifPresent(user -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um usuario com esse login.");
                });

        AppUser user = new AppUser(
                request.username().trim(),
                passwordEncoder.encode(request.password()),
                request.role(),
                request.enabled(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        return AppUserResponse.fromEntity(appUserRepository.save(user));
    }

    @Transactional
    public AppUserResponse updateUser(Long id, UpdateAppUserRequest request) {
        AppUser user = getUser(id);

        appUserRepository.findByUsername(request.username().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um usuario com esse login.");
                });

        user.update(request.username().trim(), request.role(), request.enabled());
        if (StringUtils.hasText(request.password())) {
            user.updatePasswordHash(passwordEncoder.encode(request.password()));
        }

        return AppUserResponse.fromEntity(appUserRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        AppUser user = getUser(id);
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O usuario admin padrao nao pode ser removido.");
        }

        appUserRepository.delete(user);
    }

    private AppUser getUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
    }
}
