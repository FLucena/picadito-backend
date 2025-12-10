package com.techlab.picadito.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para aplicar rate limiting a endpoints de autenticación
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final AuthenticationAuditService auditService;

    public RateLimitingFilter(
            RateLimitingService rateLimitingService,
            AuthenticationAuditService auditService) {
        this.rateLimitingService = rateLimitingService;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ipAddress = getClientIpAddress(request);

        // Aplicar rate limiting solo a endpoints de autenticación
        if (path.startsWith("/api/auth/login")) {
            if (!rateLimitingService.tryConsumeLogin(ipAddress)) {
                auditService.logRateLimitExceeded("/api/auth/login", ipAddress, "IP");
                response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos de login. Por favor, intenta más tarde.\",\"status\":429}"
                );
                return;
            }
        } else if (path.startsWith("/api/auth/register")) {
            if (!rateLimitingService.tryConsumeRegister(ipAddress)) {
                auditService.logRateLimitExceeded("/api/auth/register", ipAddress, "IP");
                response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos de registro. Por favor, intenta más tarde.\",\"status\":429}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Tomar la primera IP de la lista (IP original del cliente)
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

