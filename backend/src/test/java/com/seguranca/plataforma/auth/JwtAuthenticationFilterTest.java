package com.seguranca.plataforma.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveSeguirFluxoQuandoNaoHouverBearerToken() throws Exception {
        // Sem cabecalho Authorization o filtro nao pode interferir no restante da cadeia.
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, appUserRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtTokenService, appUserRepository);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveAutenticarUsuarioQuandoOTokenForValidoEAVersaoBater() throws Exception {
        // O contrato real da API depende da versao do token bater com a versao salva no banco.
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, appUserRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AppUser storedUser = new AppUser(
                "admin",
                "{noop}senha",
                AppUserRole.ADMIN,
                true,
                OffsetDateTime.parse("2026-03-23T10:00:00Z")
        );
        storedUser.bumpTokenVersion();

        when(jwtTokenService.extractUsername("token-valido")).thenReturn("admin");
        when(jwtTokenService.extractTokenVersion("token-valido")).thenReturn(1);
        when(jwtTokenService.isValid("token-valido", "admin")).thenReturn(true);
        when(jwtTokenService.extractRoles("token-valido")).thenReturn(List.of("ROLE_ADMIN"));
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(storedUser));

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("admin", authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveRecusarTokenQuandoAVersaoDoBancoForDiferente() throws Exception {
        // Se a sessao foi invalidada, o token antigo precisa cair sem deixar autenticacao residual.
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, appUserRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-antigo");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AppUser storedUser = new AppUser(
                "admin",
                "{noop}senha",
                AppUserRole.ADMIN,
                true,
                OffsetDateTime.parse("2026-03-23T10:00:00Z")
        );
        storedUser.bumpTokenVersion();
        storedUser.bumpTokenVersion();

        when(jwtTokenService.extractUsername("token-antigo")).thenReturn("admin");
        when(jwtTokenService.extractTokenVersion("token-antigo")).thenReturn(1);
        when(jwtTokenService.isValid("token-antigo", "admin")).thenReturn(true);
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(storedUser));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenService, never()).extractRoles("token-antigo");
    }
}
