package com.techlab.picadito.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Valida que los secretos críticos estén configurados correctamente
 * y no se usen valores por defecto inseguros en producción
 */
@Component
public class SecretsValidator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SecretsValidator.class);

    private final Environment environment;
    private final String jwtSecret;
    private final String activeProfile;

    // Valores por defecto inseguros que no deben usarse en producción
    private static final String[] INSECURE_JWT_SECRETS = {
        "MiClaveSecretaSuperSeguraParaJWTTokenGeneration2024",
        "MiClaveSecretaSuperSeguraParaJWTTokenGeneration2024CambiarEnProduccion",
        "secret",
        "changeme",
        "default",
        "test"
    };

    public SecretsValidator(
            Environment environment,
            @Value("${jwt.secret:}") String jwtSecret,
            @Value("${spring.profiles.active:}") String activeProfile) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.activeProfile = activeProfile;
    }

    @Override
    public void run(String... args) {
        List<String> errors = new ArrayList<>();
        boolean isProduction = isProductionProfile();

        // Validar JWT Secret
        validateJwtSecret(errors, isProduction);

        // Validar que JWT_SECRET esté definido como variable de entorno en producción
        if (isProduction) {
            String envJwtSecret = environment.getProperty("JWT_SECRET");
            if (envJwtSecret == null || envJwtSecret.trim().isEmpty()) {
                errors.add("JWT_SECRET debe estar definido como variable de entorno en producción");
            }
        }

        if (!errors.isEmpty()) {
            logger.error("==========================================");
            logger.error("ERRORES DE CONFIGURACIÓN DE SEGURIDAD:");
            logger.error("==========================================");
            errors.forEach(error -> logger.error("  - {}", error));
            logger.error("==========================================");
            
            if (isProduction) {
                throw new IllegalStateException(
                    "La aplicación no puede iniciar con configuración de seguridad inválida. " +
                    "Por favor, corrige los errores antes de continuar."
                );
            } else {
                logger.warn("ADVERTENCIA: La aplicación está usando valores por defecto inseguros. " +
                           "Esto es aceptable solo en desarrollo.");
            }
        } else {
            logger.info("Validación de secretos completada exitosamente");
        }
    }

    private void validateJwtSecret(List<String> errors, boolean isProduction) {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            errors.add("JWT secret no está configurado");
            return;
        }

        // Verificar que no sea un valor por defecto inseguro
        String trimmedSecret = jwtSecret.trim();
        for (String insecureSecret : INSECURE_JWT_SECRETS) {
            if (trimmedSecret.equals(insecureSecret)) {
                if (isProduction) {
                    errors.add(String.format(
                        "JWT secret no puede usar el valor por defecto inseguro: '%s'. " +
                        "Debe configurarse JWT_SECRET como variable de entorno con un valor seguro.",
                        insecureSecret
                    ));
                } else {
                    logger.warn("JWT secret está usando un valor por defecto inseguro. " +
                               "Asegúrate de usar un valor seguro en producción.");
                }
                break;
            }
        }

        // Validar longitud mínima
        if (trimmedSecret.length() < 32) {
            errors.add(String.format(
                "JWT secret debe tener al menos 32 caracteres. Longitud actual: %d",
                trimmedSecret.length()
            ));
        }
    }

    private boolean isProductionProfile() {
        return activeProfile != null && 
               (activeProfile.contains("prod") || activeProfile.contains("production"));
    }
}

