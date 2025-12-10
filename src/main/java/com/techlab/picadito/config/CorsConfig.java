package com.techlab.picadito.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

// Deshabilitado: Spring Security ya maneja CORS a través de SecurityConfig
// @Configuration
public class CorsConfig {
    
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://127.0.0.1:5173,https://unpicadito.vercel.app}")
    private String allowedOrigins;
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "X-Correlation-ID"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setExposedHeaders(Arrays.asList("X-Correlation-ID"));
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

