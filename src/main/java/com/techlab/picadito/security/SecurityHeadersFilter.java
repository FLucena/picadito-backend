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
 * Filtro para agregar headers de seguridad HTTP
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

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
        
        // Habilitar XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Content Security Policy (ajustar según necesidades)
        // Nota: connect-src permite conexiones al backend desde el frontend
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' data:; " +
            "connect-src 'self' https://picadito-backend.onrender.com https://unpicadito.vercel.app"
        );
        
        // Permissions Policy
        response.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=()"
        );
        
        // Strict Transport Security (solo en HTTPS)
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains"
            );
        }
        
        filterChain.doFilter(request, response);
    }
}

