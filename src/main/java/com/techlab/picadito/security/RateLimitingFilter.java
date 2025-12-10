package com.techlab.picadito.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para aplicar rate limiting a todos los endpoints
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
        String method = request.getMethod();
        String ipAddress = getClientIpAddress(request);
        
        // Omitir rate limiting para OPTIONS (preflight CORS)
        if ("OPTIONS".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Rate limiting específico para endpoints de autenticación
        if (path.startsWith("/api/auth/login")) {
            if (!rateLimitingService.tryConsumeLogin(ipAddress)) {
                auditService.logRateLimitExceeded("/api/auth/login", ipAddress, "IP");
                sendRateLimitResponse(response, "Demasiados intentos de login. Por favor, intenta más tarde.");
                return;
            }
        } else if (path.startsWith("/api/auth/register")) {
            if (!rateLimitingService.tryConsumeRegister(ipAddress)) {
                auditService.logRateLimitExceeded("/api/auth/register", ipAddress, "IP");
                sendRateLimitResponse(response, "Demasiados intentos de registro. Por favor, intenta más tarde.");
                return;
            }
        } else {
            // Rate limiting global para otros endpoints
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = authentication != null && 
                                     authentication.isAuthenticated() &&
                                     !"anonymousUser".equals(authentication.getPrincipal());
            
            String username = null;
            if (isAuthenticated && authentication != null) {
                username = authentication.getName();
            }
            
            // Aplicar rate limiting por tipo de endpoint
            boolean allowed = true;
            String endpointType = "";
            
            if (path.startsWith("/api/admin")) {
                // Endpoints de administración
                allowed = rateLimitingService.tryConsumeAdminEndpoint(ipAddress);
                endpointType = "ADMIN";
                
                // También aplicar rate limiting por usuario para admins
                if (allowed && username != null) {
                    allowed = rateLimitingService.tryConsumeUserRequest(username);
                }
            } else if (isAuthenticated && !path.startsWith("/api/auth") && 
                      !path.startsWith("/actuator") && 
                      !path.startsWith("/swagger-ui") && 
                      !path.startsWith("/v3/api-docs")) {
                // Endpoints autenticados (no admin, no auth)
                allowed = rateLimitingService.tryConsumeAuthenticatedEndpoint(ipAddress);
                endpointType = "AUTHENTICATED";
                
                // También aplicar rate limiting por usuario
                if (allowed && username != null) {
                    allowed = rateLimitingService.tryConsumeUserRequest(username);
                }
            } else if (!isAuthenticated && 
                      (path.startsWith("/api/partidos") || 
                       path.startsWith("/api/categorias") || 
                       path.startsWith("/api/sedes"))) {
                // Endpoints públicos (solo lectura)
                allowed = rateLimitingService.tryConsumePublicEndpoint(ipAddress);
                endpointType = "PUBLIC";
            }
            
            if (!allowed) {
                String identifier = username != null ? username : ipAddress;
                auditService.logRateLimitExceeded(path, identifier, endpointType);
                sendRateLimitResponse(response, "Demasiadas solicitudes. Por favor, intenta más tarde.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
    
    private void sendRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("application/json");
        response.getWriter().write(
            String.format("{\"error\":\"Too Many Requests\",\"message\":\"%s\",\"status\":429}", message)
        );
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

