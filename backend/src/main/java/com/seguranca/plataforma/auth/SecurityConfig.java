package com.seguranca.plataforma.auth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    // Centraliza autenticacao por token e regras de acesso por perfil da operacao.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/password-reset/**").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/client/portal").hasAnyRole("CLIENT", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**").hasAnyRole("RONDA", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/agents/**", "/api/vehicles/**", "/api/residents/**", "/api/shifts/**", "/api/incidents/**", "/api/resident-alerts/**").hasAnyRole("RONDA", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shifts/*/telemetry").hasAnyRole("RONDA", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shifts/*/handoff").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/agents/**", "/api/vehicles/**", "/api/residents/**", "/api/shifts/**", "/api/incidents/**", "/api/resident-alerts/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/agents/**", "/api/vehicles/**", "/api/residents/**", "/api/resident-alerts/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/shifts/**", "/api/incidents/**", "/api/resident-alerts/**").hasAnyRole("RONDA", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/agents/**", "/api/vehicles/**", "/api/residents/**", "/api/shifts/**", "/api/incidents/**", "/api/resident-alerts/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(AppUserDetailsService appUserDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
