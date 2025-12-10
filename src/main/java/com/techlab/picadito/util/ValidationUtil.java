package com.techlab.picadito.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utilidades para validación y sanitización de datos
 */
@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|onerror|onload)"
    );

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|</script>|javascript:|onerror=|onload=|onclick=|onmouseover=)"
    );

    /**
     * Valida formato de email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Sanitiza string para prevenir SQL injection
     * Nota: Esta función es para uso en búsquedas. Para datos que se guardan en BD,
     * usar prepared statements en lugar de concatenación de strings.
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // Remover caracteres peligrosos
        return input.trim()
                .replace("'", "''")
                .replace(";", "")
                .replace("--", "")
                .replace("/*", "")
                .replace("*/", "");
    }

    /**
     * Sanitiza string para prevenir XSS
     * Usa HTML escaping para convertir caracteres peligrosos a entidades HTML
     */
    public static String sanitizeForXSS(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }
    
    /**
     * Sanitiza string para uso en búsquedas (previene SQL injection y XSS)
     */
    public static String sanitizeForSearch(String input) {
        if (input == null) {
            return null;
        }
        String sanitized = sanitizeInput(input);
        return sanitizeForXSS(sanitized);
    }

    /**
     * Valida que un string no contenga patrones de SQL injection
     */
    public static boolean containsSQLInjection(String input) {
        if (input == null) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Valida que un string no contenga patrones de XSS
     */
    public static boolean containsXSS(String input) {
        if (input == null) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Limita la longitud de un string
     */
    public static String limitLength(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        if (input.length() > maxLength) {
            return input.substring(0, maxLength);
        }
        return input;
    }
}

