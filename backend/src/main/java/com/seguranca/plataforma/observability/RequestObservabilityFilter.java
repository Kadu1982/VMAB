package com.seguranca.plataforma.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestObservabilityFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestObservabilityFilter.class);
    private static final List<String> SKIPPED_PATH_PREFIXES = List.of(
            "/actuator/health",
            "/actuator/metrics",
            "/actuator/prometheus",
            "/api/events/stream"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !StringUtils.hasText(path) || path.equals("/error") || path.equals("/favicon.ico")
                || SKIPPED_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);
        long startedAtNanos = System.nanoTime();
        Throwable failure = null;

        response.setHeader("X-Correlation-Id", correlationId);
        MDC.put("correlationId", correlationId);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("httpPath", request.getRequestURI());
        MDC.put("clientIp", resolveClientIp(request));

        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            failure = exception;
            if (exception instanceof ServletException servletException) {
                throw servletException;
            }
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ServletException(exception);
        } finally {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = resolveUsername(authentication);
            if (StringUtils.hasText(username)) {
                MDC.put("username", username);
            }
            String roles = resolveRoles(authentication);
            if (StringUtils.hasText(roles)) {
                MDC.put("roles", roles);
            }

            int status = failure == null ? response.getStatus() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            MDC.put("httpStatus", Integer.toString(status));
            MDC.put("durationMs", Long.toString(durationMs));

            if (failure != null || status >= 500) {
                LOGGER.error("request failed");
            } else if (status >= 400 || durationMs >= 1000) {
                LOGGER.warn("request completed");
            } else {
                LOGGER.info("request completed");
            }

            MDC.clear();
        }
    }

    private static String resolveCorrelationId(HttpServletRequest request) {
        String headerValue = request.getHeader("X-Correlation-Id");
        if (StringUtils.hasText(headerValue)) {
            return headerValue.trim();
        }
        return UUID.randomUUID().toString();
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return Arrays.stream(forwardedFor.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(request.getRemoteAddr());
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private static String resolveUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String username && StringUtils.hasText(username)) {
            return username;
        }
        return authentication.getName();
    }

    private static String resolveRoles(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(StringUtils::hasText)
                .map(authority -> authority.toUpperCase(Locale.ROOT))
                .collect(Collectors.joining(","));
    }
}


