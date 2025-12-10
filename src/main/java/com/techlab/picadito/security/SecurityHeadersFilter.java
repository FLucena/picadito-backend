package com.techlab.picadito.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para agregar headers de seguridad HTTP
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private final Environment environment;
    
    @Value("${app.security.csp.report-uri:}")
    private String cspReportUri;
    
    @Value("${app.security.csp.allowed-sources:}")
    private String allowedSources;

    public SecurityHeadersFilter(Environment environment) {
        this.environment = environment;
    }
    
    private boolean isProduction() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (profile.contains("prod") || profile.contains("production")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Prevenir clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // Prevenir MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Habilitar XSS protection (deprecated pero aún útil para navegadores antiguos)
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Content Security Policy (más restrictivo en producción)
        response.setHeader("Content-Security-Policy", buildCSP());
        
        // Permissions Policy
        response.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=(), gyroscope=()"
        );
        
        // Strict Transport Security (solo en HTTPS)
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload"
            );
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Construye el Content Security Policy según el perfil
     */
    private String buildCSP() {
        StringBuilder csp = new StringBuilder();
        
        if (isProduction()) {
            // CSP más restrictivo para producción
            csp.append("default-src 'self'; ");
            csp.append("script-src 'self'; "); // Sin unsafe-inline ni unsafe-eval
            csp.append("style-src 'self' 'unsafe-inline'; "); // unsafe-inline necesario para estilos inline
            csp.append("img-src 'self' data: https:; ");
            csp.append("font-src 'self' data:; ");
            csp.append("connect-src 'self'");
            
            // Agregar fuentes permitidas si están configuradas
            if (allowedSources != null && !allowedSources.trim().isEmpty()) {
                csp.append(" ").append(allowedSources);
            }
            csp.append("; ");
            csp.append("frame-ancestors 'none'; ");
            csp.append("base-uri 'self'; ");
            csp.append("form-action 'self'; ");
            csp.append("upgrade-insecure-requests; ");
        } else {
            // CSP más permisivo para desarrollo (permite herramientas de desarrollo)
            csp.append("default-src 'self'; ");
            csp.append("script-src 'self' 'unsafe-inline' 'unsafe-eval'; "); // Permitir para desarrollo
            csp.append("style-src 'self' 'unsafe-inline'; ");
            csp.append("img-src 'self' data: https:; ");
            csp.append("font-src 'self' data:; ");
            csp.append("connect-src 'self'");
            
            if (allowedSources != null && !allowedSources.trim().isEmpty()) {
                csp.append(" ").append(allowedSources);
            }
            csp.append("; ");
        }
        
        // Agregar report-uri si está configurado
        if (cspReportUri != null && !cspReportUri.trim().isEmpty()) {
            csp.append("report-uri ").append(cspReportUri).append("; ");
            csp.append("report-to csp-endpoint; ");
        }
        
        return csp.toString().trim();
    }
}

