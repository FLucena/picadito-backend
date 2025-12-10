package com.techlab.picadito.config;

import com.techlab.picadito.security.AuthenticationAuditService;
import com.techlab.picadito.security.TokenBlacklistService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Componente para exponer métricas de seguridad a través de Actuator
 */
@Component
@Endpoint(id = "security")
public class SecurityMetrics {

    private final AuthenticationAuditService auditService;
    private final TokenBlacklistService tokenBlacklistService;
    private final MeterRegistry meterRegistry;

    public SecurityMetrics(
            AuthenticationAuditService auditService,
            TokenBlacklistService tokenBlacklistService,
            MeterRegistry meterRegistry) {
        this.auditService = auditService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.meterRegistry = meterRegistry;
        
        // Registrar métricas de seguridad
        registerMetrics();
    }

    /**
     * Endpoint de Actuator para obtener métricas de seguridad
     */
    @ReadOperation
    public Map<String, Object> getSecurityMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        AuthenticationAuditService.SecurityMetrics auditMetrics = auditService.getSecurityMetrics();
        
        metrics.put("failedLoginAttempts", Map.of(
            "uniqueIps", auditMetrics.getUniqueIpsWithFailedAttempts(),
            "uniqueEmails", auditMetrics.getUniqueEmailsWithFailedAttempts(),
            "totalByIp", auditMetrics.getTotalFailedAttemptsByIp(),
            "totalByEmail", auditMetrics.getTotalFailedAttemptsByEmail()
        ));
        
        metrics.put("revokedTokens", tokenBlacklistService.getRevokedTokenCount());
        
        return metrics;
    }

    /**
     * Registra métricas en Micrometer para monitoreo
     */
    private void registerMetrics() {
        Gauge.builder("security.revoked.tokens.count", tokenBlacklistService, 
                service -> service.getRevokedTokenCount())
            .description("Número de tokens JWT revocados")
            .register(meterRegistry);
    }
}

