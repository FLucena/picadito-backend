package com.techlab.picadito.security;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar la blacklist de tokens JWT revocados
 */
@Service
public class TokenBlacklistService {

    // Usar un Set en memoria para tokens revocados
    // En producción, considerar usar Redis o base de datos para persistencia
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Agrega un token a la blacklist
     */
    @CacheEvict(value = "tokenBlacklist", key = "#token")
    public void revokeToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            revokedTokens.add(token);
        }
    }

    /**
     * Verifica si un token está en la blacklist
     */
    @Cacheable(value = "tokenBlacklist", key = "#token")
    public boolean isTokenRevoked(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        return revokedTokens.contains(token);
    }

    /**
     * Limpia tokens antiguos de la blacklist
     * Nota: En producción, implementar limpieza basada en expiración de tokens
     */
    public void cleanup() {
        // Por ahora, mantener todos los tokens hasta que expiren
        // En producción, implementar limpieza periódica basada en tiempo de expiración
    }

    /**
     * Obtiene el número de tokens revocados (para métricas)
     */
    public int getRevokedTokenCount() {
        return revokedTokens.size();
    }
}

