package com.techlab.picadito.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Filtro para sanitizar automáticamente los parámetros de request
 * para prevenir XSS y SQL injection
 */
@Component
public class InputSanitizationFilter extends OncePerRequestFilter {

    // Patrones peligrosos para SQL injection
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|onerror|onload|xp_|sp_)"
    );

    // Patrones peligrosos para XSS
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<script|</script>|javascript:|onerror=|onload=|onclick=|onmouseover=|onfocus=|onblur=|eval\\(|expression\\()"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Sanitizar parámetros de query string
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null) {
                        // Validar y rechazar si contiene patrones peligrosos
                        if (containsSQLInjection(values[i]) || containsXSS(values[i])) {
                            response.setStatus(400);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                "{\"error\":\"Bad Request\",\"message\":\"La solicitud contiene caracteres o patrones no permitidos.\",\"status\":400}"
                            );
                            return;
                        }
                        // Sanitizar el valor
                        values[i] = sanitizeInput(values[i]);
                    }
                }
            }
        }

        // Para parámetros de path, validar pero no sanitizar (ya están en la URL)
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && (containsSQLInjection(pathInfo) || containsXSS(pathInfo))) {
            response.setStatus(400);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Bad Request\",\"message\":\"La URL contiene caracteres o patrones no permitidos.\",\"status\":400}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica si un string contiene patrones de SQL injection
     */
    private boolean containsSQLInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Verifica si un string contiene patrones de XSS
     */
    private boolean containsXSS(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Sanitiza un input removiendo caracteres peligrosos
     * Nota: Para campos de texto que se mostrarán en HTML, usar HtmlUtils.htmlEscape
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remover caracteres de control y espacios al inicio/final
        String sanitized = input.trim();
        
        // Remover caracteres peligrosos para SQL (pero mantener la funcionalidad básica)
        sanitized = sanitized.replace("'", "")
                            .replace(";", "")
                            .replace("--", "")
                            .replace("/*", "")
                            .replace("*/", "")
                            .replace("xp_", "")
                            .replace("sp_", "");
        
        // Escapar HTML para prevenir XSS (pero permitir caracteres normales)
        // HtmlUtils.htmlEscape nunca retorna null, pero el análisis estático requiere verificación
        @SuppressWarnings("null")
        String escaped = HtmlUtils.htmlEscape(sanitized);
        return escaped;
    }
}

