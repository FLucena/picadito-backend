package com.techlab.picadito.config;

import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.partido.PartidoRepository;
import com.techlab.picadito.alerta.AlertaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private AlertaService alertaService;

    /**
     * Ejecuta cada hora para verificar partidos próximos (24-48 horas antes)
     * y generar alertas para los participantes
     */
    @Scheduled(fixedRate = 3600000) // Cada hora (3600000 ms)
    public void verificarPartidosProximos() {
        logger.info("Ejecutando verificación de partidos próximos");
        
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en24Horas = ahora.plusHours(24);
        LocalDateTime en48Horas = ahora.plusHours(48);
        
        // Buscar partidos que están entre 24 y 48 horas en el futuro
        List<Partido> partidosProximos = partidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPartido.DISPONIBLE || 
                           p.getEstado() == EstadoPartido.COMPLETO)
                .filter(p -> p.getFechaHora().isAfter(en24Horas) && 
                           p.getFechaHora().isBefore(en48Horas))
                .collect(Collectors.toList());
        
        for (Partido partido : partidosProximos) {
            // Por ahora, crear alerta general sin usuario específico
            // En producción, se debería crear una alerta por cada usuario participante
            if (!partido.getParticipantes().isEmpty()) {
                try {
                    alertaService.crearAlertaPartidoProximo(partido, null);
                } catch (Exception e) {
                    logger.error("Error al crear alerta de partido próximo para partido {}: {}", 
                            partido.getId(), e.getMessage());
                }
            }
        }
        
        logger.info("Verificación de partidos próximos completada. Partidos encontrados: {}", 
                partidosProximos.size());
    }

    /**
     * Ejecuta diariamente a las 2 AM para limpiar alertas antiguas (más de 30 días)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Cada día a las 2 AM
    public void limpiarAlertasAntiguas() {
        logger.info("Ejecutando limpieza de alertas antiguas");
        try {
            alertaService.eliminarAlertasAntiguas(30);
            logger.info("Limpieza de alertas antiguas completada");
        } catch (Exception e) {
            logger.error("Error al limpiar alertas antiguas: {}", e.getMessage());
        }
    }
}

