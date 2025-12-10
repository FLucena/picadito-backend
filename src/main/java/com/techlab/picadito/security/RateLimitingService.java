package com.techlab.picadito.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar rate limiting usando Bucket4j
 */
@Service
public class RateLimitingService {

    // Buckets por IP para endpoints de autenticación
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    
    // Buckets por IP para endpoints de registro
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    
    // Buckets por email para intentos de login
    private final Map<String, Bucket> loginAttemptBuckets = new ConcurrentHashMap<>();

    // Configuración: 5 intentos por minuto para login
    private static final int LOGIN_ATTEMPTS_PER_MINUTE = 5;
    
    // Configuración: 3 registros por hora por IP
    private static final int REGISTERS_PER_HOUR = 3;
    
    // Configuración: 10 requests por minuto para otros endpoints de auth
    private static final int AUTH_REQUESTS_PER_MINUTE = 10;

    /**
     * Obtiene o crea un bucket para rate limiting de login por IP
     */
    public Bucket getLoginBucket(String ipAddress) {
        return authBuckets.computeIfAbsent(ipAddress, k -> {
            Bandwidth limit = Bandwidth.classic(
                LOGIN_ATTEMPTS_PER_MINUTE,
                Refill.intervally(LOGIN_ATTEMPTS_PER_MINUTE, Duration.ofMinutes(1))
            );
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Obtiene o crea un bucket para rate limiting de registro por IP
     */
    public Bucket getRegisterBucket(String ipAddress) {
        return registerBuckets.computeIfAbsent(ipAddress, k -> {
            Bandwidth limit = Bandwidth.classic(
                REGISTERS_PER_HOUR,
                Refill.intervally(REGISTERS_PER_HOUR, Duration.ofHours(1))
            );
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Obtiene o crea un bucket para rate limiting de intentos de login por email
     */
    public Bucket getLoginAttemptBucket(String email) {
        return loginAttemptBuckets.computeIfAbsent(email.toLowerCase(), k -> {
            Bandwidth limit = Bandwidth.classic(
                LOGIN_ATTEMPTS_PER_MINUTE,
                Refill.intervally(LOGIN_ATTEMPTS_PER_MINUTE, Duration.ofMinutes(1))
            );
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Verifica si una IP puede hacer una petición de login
     */
    public boolean tryConsumeLogin(String ipAddress) {
        Bucket bucket = getLoginBucket(ipAddress);
        return bucket.tryConsume(1);
    }

    /**
     * Verifica si una IP puede hacer una petición de registro
     */
    public boolean tryConsumeRegister(String ipAddress) {
        Bucket bucket = getRegisterBucket(ipAddress);
        return bucket.tryConsume(1);
    }

    /**
     * Verifica si un email puede intentar hacer login
     */
    public boolean tryConsumeLoginAttempt(String email) {
        Bucket bucket = getLoginAttemptBucket(email);
        return bucket.tryConsume(1);
    }

    /**
     * Obtiene los tokens disponibles para una IP en login
     */
    public long getAvailableLoginTokens(String ipAddress) {
        Bucket bucket = getLoginBucket(ipAddress);
        return bucket.getAvailableTokens();
    }

    /**
     * Obtiene los tokens disponibles para una IP en registro
     */
    public long getAvailableRegisterTokens(String ipAddress) {
        Bucket bucket = getRegisterBucket(ipAddress);
        return bucket.getAvailableTokens();
    }

    /**
     * Limpia buckets antiguos (útil para liberar memoria)
     */
    public void cleanup() {
        // En producción, podrías implementar una limpieza periódica
        // Por ahora, los buckets se mantienen en memoria
    }
}

