package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.auth.AppUser;
import com.seguranca.plataforma.auth.AppUserRepository;
import com.seguranca.plataforma.auth.JwtTokenService;
import com.seguranca.plataforma.operations.service.OperationsRealtimeService;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
public class OperationsEventsController {
    // Abre o stream SSE autenticado por token para o painel reagir em tempo quase real.

    private final JwtTokenService jwtTokenService;
    private final AppUserRepository appUserRepository;
    private final OperationsRealtimeService operationsRealtimeService;

    public OperationsEventsController(
            JwtTokenService jwtTokenService,
            AppUserRepository appUserRepository,
            OperationsRealtimeService operationsRealtimeService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.appUserRepository = appUserRepository;
        this.operationsRealtimeService = operationsRealtimeService;
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam String token) {
        if (!StringUtils.hasText(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token do stream ausente.");
        }

        try {
            String username = jwtTokenService.extractUsername(token);
            int tokenVersion = jwtTokenService.extractTokenVersion(token);
            AppUser user = appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario do stream não encontrado."));

            if (!user.isEnabled() || !jwtTokenService.isValid(token, username) || user.getTokenVersion() != tokenVersion) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token do stream invalido.");
            }

            return operationsRealtimeService.subscribe(username);
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token do stream invalido.");
        }
    }
}


