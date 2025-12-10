package com.techlab.picadito.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio para registrar eventos de autenticación para auditoría
 * y detectar patrones sospechosos
 */
@Service
public class AuthenticationAuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // Contadores para detección de patrones sospechosos
    private final Map<String, AtomicInteger> failedLoginAttemptsByIp = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failedLoginAttemptsByEmail = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastFailedLoginByIp = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastFailedLoginByEmail = new ConcurrentHashMap<>();
    
    // Umbrales para alertas
    private static final int SUSPICIOUS_FAILED_ATTEMPTS_THRESHOLD = 10;
    private static final int SUSPICIOUS_FAILED_ATTEMPTS_WINDOW_MINUTES = 15;

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
    
    /**
     * Detecta patrones sospechosos en intentos de login fallidos
     */
    public void detectSuspiciousPatterns(String email, String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        
        // Limpiar intentos antiguos
        cleanupOldAttempts(now);
        
        // Incrementar contadores
        int ipAttempts = failedLoginAttemptsByIp.computeIfAbsent(ipAddress, k -> new AtomicInteger(0)).incrementAndGet();
        int emailAttempts = failedLoginAttemptsByEmail.computeIfAbsent(email.toLowerCase(), k -> new AtomicInteger(0)).incrementAndGet();
        
        lastFailedLoginByIp.put(ipAddress, now);
        lastFailedLoginByEmail.put(email.toLowerCase(), now);
        
        // Detectar patrones sospechosos
        if (ipAttempts >= SUSPICIOUS_FAILED_ATTEMPTS_THRESHOLD) {
            String correlationId = MDC.get("correlationId");
            auditLogger.error("SUSPICIOUS_ACTIVITY_DETECTED|type=IP_BRUTE_FORCE|ip={}|attempts={}|timestamp={}|correlationId={}",
                ipAddress, ipAttempts, now.format(formatter), correlationId);
        }
        
        if (emailAttempts >= SUSPICIOUS_FAILED_ATTEMPTS_THRESHOLD) {
            String correlationId = MDC.get("correlationId");
            auditLogger.error("SUSPICIOUS_ACTIVITY_DETECTED|type=EMAIL_BRUTE_FORCE|email={}|attempts={}|timestamp={}|correlationId={}",
                email, emailAttempts, now.format(formatter), correlationId);
        }
    }
    
    /**
     * Limpia intentos antiguos fuera de la ventana de tiempo
     */
    private void cleanupOldAttempts(LocalDateTime now) {
        LocalDateTime threshold = now.minusMinutes(SUSPICIOUS_FAILED_ATTEMPTS_WINDOW_MINUTES);
        
        lastFailedLoginByIp.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(threshold)) {
                failedLoginAttemptsByIp.remove(entry.getKey());
                return true;
            }
            return false;
        });
        
        lastFailedLoginByEmail.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(threshold)) {
                failedLoginAttemptsByEmail.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Resetea contadores después de un login exitoso
     */
    public void resetFailedAttempts(String email, String ipAddress) {
        failedLoginAttemptsByIp.remove(ipAddress);
        failedLoginAttemptsByEmail.remove(email.toLowerCase());
        lastFailedLoginByIp.remove(ipAddress);
        lastFailedLoginByEmail.remove(email.toLowerCase());
    }
    
    /**
     * Obtiene métricas de seguridad
     */
    public SecurityMetrics getSecurityMetrics() {
        return new SecurityMetrics(
            failedLoginAttemptsByIp.size(),
            failedLoginAttemptsByEmail.size(),
            failedLoginAttemptsByIp.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum(),
            failedLoginAttemptsByEmail.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum()
        );
    }
    
    /**
     * Clase para métricas de seguridad
     */
    public static class SecurityMetrics {
        private final int uniqueIpsWithFailedAttempts;
        private final int uniqueEmailsWithFailedAttempts;
        private final int totalFailedAttemptsByIp;
        private final int totalFailedAttemptsByEmail;
        
        public SecurityMetrics(int uniqueIpsWithFailedAttempts, int uniqueEmailsWithFailedAttempts,
                              int totalFailedAttemptsByIp, int totalFailedAttemptsByEmail) {
            this.uniqueIpsWithFailedAttempts = uniqueIpsWithFailedAttempts;
            this.uniqueEmailsWithFailedAttempts = uniqueEmailsWithFailedAttempts;
            this.totalFailedAttemptsByIp = totalFailedAttemptsByIp;
            this.totalFailedAttemptsByEmail = totalFailedAttemptsByEmail;
        }
        
        public int getUniqueIpsWithFailedAttempts() {
            return uniqueIpsWithFailedAttempts;
        }
        
        public int getUniqueEmailsWithFailedAttempts() {
            return uniqueEmailsWithFailedAttempts;
        }
        
        public int getTotalFailedAttemptsByIp() {
            return totalFailedAttemptsByIp;
        }
        
        public int getTotalFailedAttemptsByEmail() {
            return totalFailedAttemptsByEmail;
        }
    }
}

