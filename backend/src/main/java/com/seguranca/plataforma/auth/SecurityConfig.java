package com.seguranca.plataforma.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    // Centraliza autenticacao basica e regras de acesso por perfil da operacao.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/client/portal").hasAnyRole("CLIENT", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**").hasAnyRole("RONDA", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/agents/**", "/api/vehicles/**", "/api/residents/**", "/api/shifts/**", "/api/incidents/**").hasAnyRole("RONDA", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shifts/*/telemetry").hasAnyRole("RONDA", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/agents/**", "/api/vehicles/**", "/api/residents/**", "/api/shifts/**", "/api/incidents/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/agents/**", "/api/vehicles/**", "/api/residents/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/shifts/**", "/api/incidents/**").hasAnyRole("RONDA", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/agents/**", "/api/vehicles/**", "/api/residents/**", "/api/shifts/**", "/api/incidents/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder
    ) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        UserDetails supervisor = User.builder()
                .username("supervisor")
                .password(passwordEncoder.encode("super123"))
                .roles("SUPERVISOR")
                .build();
        UserDetails client = User.builder()
                .username("cliente")
                .password(passwordEncoder.encode("cliente123"))
                .roles("CLIENT")
                .build();
        UserDetails ronda = User.builder()
                .username("ronda")
                .password(passwordEncoder.encode("ronda123"))
                .roles("RONDA")
                .build();
        return new InMemoryUserDetailsManager(admin, supervisor, client, ronda);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
