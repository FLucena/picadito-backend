package com.techlab.picadito.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validador de contraseñas con políticas de seguridad
 */
@Component
public class PasswordValidator {

    // Mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número
    private static final Pattern STRONG_PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    // Contraseñas comunes que deben evitarse
    private static final String[] COMMON_PASSWORDS = {
        "password", "12345678", "qwerty", "abc123", "password123",
        "admin", "letmein", "welcome", "monkey", "1234567890",
        "1234567", "sunshine", "princess", "azerty", "trustno1"
    };

    /**
     * Valida que una contraseña cumpla con los requisitos de seguridad
     * 
     * @param password Contraseña a validar
     * @return true si la contraseña es válida
     * @throws IllegalArgumentException si la contraseña no cumple los requisitos
     */
    public void validate(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        if (password.length() > 100) {
            throw new IllegalArgumentException("La contraseña no puede tener más de 100 caracteres");
        }

        // Verificar que no sea una contraseña común
        String lowerPassword = password.toLowerCase();
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.contains(common)) {
                throw new IllegalArgumentException(
                    "La contraseña es demasiado común. Por favor, elige una contraseña más segura"
                );
            }
        }

        // Verificar complejidad (opcional pero recomendado)
        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "La contraseña debe contener al menos una letra mayúscula, una minúscula y un número"
            );
        }
    }

    /**
     * Valida contraseña con política más estricta (para producción)
     */
    public void validateStrict(String password) {
        validate(password);

        // Verificaciones adicionales
        if (password.length() < 12) {
            throw new IllegalArgumentException(
                "Para mayor seguridad, la contraseña debe tener al menos 12 caracteres"
            );
        }

        // Verificar caracteres especiales (opcional)
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new IllegalArgumentException(
                "La contraseña debe contener al menos un carácter especial"
            );
        }
    }
}

