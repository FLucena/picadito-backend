package com.techlab.picadito.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para registrar eventos de autenticación para auditoría
 */
@Service
public class AuthenticationAuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Registra un intento de login exitoso
     */
    public void logSuccessfulLogin(String email, String ipAddress) {
        String correlationId = MDC.get("correlationId");
        auditLogger.info("LOGIN_SUCCESS|email={}|ip={}|timestamp={}|correlationId={}",
            email, ipAddress, LocalDateTime.now().format(formatter), correlationId);
    }

    /**
     * Registra un intento de login fallido
     */
    public void logFailedLogin(String email, String ipAddress, String reason) {
        String correlationId = MDC.get("correlationId");
        auditLogger.warn("LOGIN_FAILED|email={}|ip={}|reason={}|timestamp={}|correlationId={}",
            email, ipAddress, reason, LocalDateTime.now().format(formatter), correlationId);
    }

    /**
     * Registra un registro de usuario exitoso
     */
    public void logSuccessfulRegistration(String email, String ipAddress) {
        String correlationId = MDC.get("correlationId");
        auditLogger.info("REGISTRATION_SUCCESS|email={}|ip={}|timestamp={}|correlationId={}",
            email, ipAddress, LocalDateTime.now().format(formatter), correlationId);
    }

    /**
     * Registra un intento de registro fallido
     */
    public void logFailedRegistration(String email, String ipAddress, String reason) {
        String correlationId = MDC.get("correlationId");
        auditLogger.warn("REGISTRATION_FAILED|email={}|ip={}|reason={}|timestamp={}|correlationId={}",
            email, ipAddress, reason, LocalDateTime.now().format(formatter), correlationId);
    }

    /**
     * Registra un bloqueo de cuenta
     */
    public void logAccountLocked(String email, String ipAddress, int failedAttempts) {
        String correlationId = MDC.get("correlationId");
        auditLogger.error("ACCOUNT_LOCKED|email={}|ip={}|failedAttempts={}|timestamp={}|correlationId={}",
            email, ipAddress, failedAttempts, LocalDateTime.now().format(formatter), correlationId);
    }

    /**
     * Registra un desbloqueo de cuenta
     */
    public void logAccountUnlocked(String email) {
        String correlationId = MDC.get("correlationId");
        auditLogger.info("ACCOUNT_UNLOCKED|email={}|timestamp={}|correlationId={}",
            email, LocalDateTime.now().format(formatter), correlationId);
    }

    /**
     * Registra un intento de acceso con token inválido
     */
    public void logInvalidToken(String ipAddress, String reason) {
        String correlationId = MDC.get("correlationId");
        auditLogger.warn("INVALID_TOKEN|ip={}|reason={}|timestamp={}|correlationId={}",
            ipAddress, reason, LocalDateTime.now().format(formatter), correlationId);
    }

    /**
     * Registra rate limiting aplicado
     */
    public void logRateLimitExceeded(String endpoint, String identifier, String type) {
        String correlationId = MDC.get("correlationId");
        auditLogger.warn("RATE_LIMIT_EXCEEDED|endpoint={}|identifier={}|type={}|timestamp={}|correlationId={}",
            endpoint, identifier, type, LocalDateTime.now().format(formatter), correlationId);
    }
}

