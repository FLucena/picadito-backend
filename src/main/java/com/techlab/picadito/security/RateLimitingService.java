package com.techlab.picadito.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
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
    
    // Buckets por IP para endpoints públicos (GET)
    private final Map<String, Bucket> publicEndpointsBuckets = new ConcurrentHashMap<>();
    
    // Buckets por IP para endpoints autenticados
    private final Map<String, Bucket> authenticatedEndpointsBuckets = new ConcurrentHashMap<>();
    
    // Buckets por usuario autenticado
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    
    // Buckets por IP para endpoints de administración
    private final Map<String, Bucket> adminEndpointsBuckets = new ConcurrentHashMap<>();

    // Configuración: 5 intentos por minuto para login
    private static final int LOGIN_ATTEMPTS_PER_MINUTE = 5;
    
    // Configuración: 3 registros por hora por IP
    private static final int REGISTERS_PER_HOUR = 3;
    
    // Configuración de rate limiting global (configurable)
    @Value("${app.rate-limit.public.requests-per-minute:100}")
    private int publicRequestsPerMinute;
    
    @Value("${app.rate-limit.authenticated.requests-per-minute:200}")
    private int authenticatedRequestsPerMinute;
    
    @Value("${app.rate-limit.admin.requests-per-minute:500}")
    private int adminRequestsPerMinute;
    
    @Value("${app.rate-limit.user.requests-per-minute:300}")
    private int userRequestsPerMinute;

    /**
     * Obtiene o crea un bucket para rate limiting de login por IP
     */
    public Bucket getLoginBucket(String ipAddress) {
        return authBuckets.computeIfAbsent(ipAddress, k -> {
            Bandwidth limit = Bandwidth.builder()
                .capacity(LOGIN_ATTEMPTS_PER_MINUTE)
                .refillIntervally(LOGIN_ATTEMPTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Obtiene o crea un bucket para rate limiting de registro por IP
     */
    public Bucket getRegisterBucket(String ipAddress) {
        return registerBuckets.computeIfAbsent(ipAddress, k -> {
            Bandwidth limit = Bandwidth.builder()
                .capacity(REGISTERS_PER_HOUR)
                .refillIntervally(REGISTERS_PER_HOUR, Duration.ofHours(1))
                .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Obtiene o crea un bucket para rate limiting de intentos de login por email
     */
    public Bucket getLoginAttemptBucket(String email) {
        return loginAttemptBuckets.computeIfAbsent(email.toLowerCase(), k -> {
            Bandwidth limit = Bandwidth.builder()
                .capacity(LOGIN_ATTEMPTS_PER_MINUTE)
                .refillIntervally(LOGIN_ATTEMPTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
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
     * Obtiene o crea un bucket para rate limiting de endpoints públicos por IP
     */
    public Bucket getPublicEndpointBucket(String ipAddress) {
        return publicEndpointsBuckets.computeIfAbsent(ipAddress, k -> {
            Bandwidth limit = Bandwidth.builder()
                .capacity(publicRequestsPerMinute)
                .refillIntervally(publicRequestsPerMinute, Duration.ofMinutes(1))
                .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Obtiene o crea un bucket para rate limiting de endpoints autenticados por IP
     */
    public Bucket getAuthenticatedEndpointBucket(String ipAddress) {
        return authenticatedEndpointsBuckets.computeIfAbsent(ipAddress, k -> {
            Bandwidth limit = Bandwidth.builder()
                .capacity(authenticatedRequestsPerMinute)
                .refillIntervally(authenticatedRequestsPerMinute, Duration.ofMinutes(1))
                .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Obtiene o crea un bucket para rate limiting de endpoints de administración por IP
     */
    public Bucket getAdminEndpointBucket(String ipAddress) {
        return adminEndpointsBuckets.computeIfAbsent(ipAddress, k -> {
            Bandwidth limit = Bandwidth.builder()
                .capacity(adminRequestsPerMinute)
                .refillIntervally(adminRequestsPerMinute, Duration.ofMinutes(1))
                .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Obtiene o crea un bucket para rate limiting por usuario autenticado
     */
    public Bucket getUserBucket(String username) {
        return userBuckets.computeIfAbsent(username.toLowerCase(), k -> {
            Bandwidth limit = Bandwidth.builder()
                .capacity(userRequestsPerMinute)
                .refillIntervally(userRequestsPerMinute, Duration.ofMinutes(1))
                .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Verifica si una IP puede hacer una petición a un endpoint público
     */
    public boolean tryConsumePublicEndpoint(String ipAddress) {
        Bucket bucket = getPublicEndpointBucket(ipAddress);
        return bucket.tryConsume(1);
    }

    /**
     * Verifica si una IP puede hacer una petición a un endpoint autenticado
     */
    public boolean tryConsumeAuthenticatedEndpoint(String ipAddress) {
        Bucket bucket = getAuthenticatedEndpointBucket(ipAddress);
        return bucket.tryConsume(1);
    }

    /**
     * Verifica si una IP puede hacer una petición a un endpoint de administración
     */
    public boolean tryConsumeAdminEndpoint(String ipAddress) {
        Bucket bucket = getAdminEndpointBucket(ipAddress);
        return bucket.tryConsume(1);
    }

    /**
     * Verifica si un usuario autenticado puede hacer una petición
     */
    public boolean tryConsumeUserRequest(String username) {
        Bucket bucket = getUserBucket(username);
        return bucket.tryConsume(1);
    }

    /**
     * Limpia buckets antiguos (útil para liberar memoria)
     */
    public void cleanup() {
        // En producción, podrías implementar una limpieza periódica
        // Por ahora, los buckets se mantienen en memoria
    }
}

