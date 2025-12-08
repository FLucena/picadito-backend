package com.techlab.picadito.service;

import com.techlab.picadito.dto.ReporteDTO;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Reserva;
import com.techlab.picadito.partido.PartidoRepository;
import com.techlab.picadito.reserva.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteService.class);

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    public ReporteDTO generarReporteVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        logger.info("Generando reporte de ventas para el período {} - {}", fechaInicio, fechaFin);
        
        ReporteDTO reporte = new ReporteDTO();
        reporte.setTipoReporte("VENTAS");
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setFechaGeneracion(LocalDateTime.now());
        
        List<Reserva> reservas = reservaRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(fechaInicio, fechaFin);
        
        Map<String, Object> datos = new HashMap<>();
        datos.put("totalReservas", reservas.size());
        
        long reservasConfirmadas = reservas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.CONFIRMADO || 
                           r.getEstado() == Reserva.EstadoReserva.FINALIZADO)
                .count();
        datos.put("reservasConfirmadas", reservasConfirmadas);
        
        double ingresosTotales = reservas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.CONFIRMADO || 
                           r.getEstado() == Reserva.EstadoReserva.FINALIZADO)
                .mapToDouble(Reserva::calcularTotal)
                .sum();
        datos.put("ingresosTotales", ingresosTotales);
        
        double promedioPorReserva = reservasConfirmadas > 0 ? ingresosTotales / reservasConfirmadas : 0.0;
        datos.put("promedioPorReserva", promedioPorReserva);
        
        reporte.setDatos(datos);
        return reporte;
    }

    public ReporteDTO generarReportePartidos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        logger.info("Generando reporte de partidos para el período {} - {}", fechaInicio, fechaFin);
        
        ReporteDTO reporte = new ReporteDTO();
        reporte.setTipoReporte("PARTIDOS");
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setFechaGeneracion(LocalDateTime.now());
        
        List<com.techlab.picadito.model.Partido> partidos = partidoRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(fechaInicio, fechaFin);
        
        Map<String, Object> datos = new HashMap<>();
        datos.put("totalPartidos", partidos.size());
        
        Map<EstadoPartido, Long> partidosPorEstado = partidos.stream()
                .collect(Collectors.groupingBy(
                        com.techlab.picadito.model.Partido::getEstado,
                        Collectors.counting()
                ));
        datos.put("partidosPorEstado", partidosPorEstado);
        
        long partidosCompletos = partidos.stream()
                .filter(com.techlab.picadito.model.Partido::estaCompleto)
                .count();
        datos.put("partidosCompletos", partidosCompletos);
        
        long partidosDisponibles = partidos.stream()
                .filter(p -> p.getEstado() == EstadoPartido.DISPONIBLE)
                .count();
        datos.put("partidosDisponibles", partidosDisponibles);
        
        double promedioParticipantes = partidos.stream()
                .mapToInt(com.techlab.picadito.model.Partido::getCantidadParticipantes)
                .average()
                .orElse(0.0);
        datos.put("promedioParticipantes", promedioParticipantes);
        
        reporte.setDatos(datos);
        return reporte;
    }

    public ReporteDTO generarReporteUsuarios(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        logger.info("Generando reporte de usuarios para el período {} - {}", fechaInicio, fechaFin);
        
        ReporteDTO reporte = new ReporteDTO();
        reporte.setTipoReporte("USUARIOS");
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setFechaGeneracion(LocalDateTime.now());
        
        List<Reserva> reservas = reservaRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(fechaInicio, fechaFin);
        
        Map<String, Object> datos = new HashMap<>();
        
        Set<Long> usuariosActivos = reservas.stream()
                .map(r -> r.getUsuario().getId())
                .collect(Collectors.toSet());
        datos.put("usuariosActivos", usuariosActivos.size());
        
        Map<Long, Long> reservasPorUsuario = reservas.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getUsuario().getId(),
                        Collectors.counting()
                ));
        datos.put("reservasPorUsuario", reservasPorUsuario);
        
        Map<Long, Double> gastosPorUsuario = reservas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.CONFIRMADO || 
                           r.getEstado() == Reserva.EstadoReserva.FINALIZADO)
                .collect(Collectors.groupingBy(
                        r -> r.getUsuario().getId(),
                        Collectors.summingDouble(Reserva::calcularTotal)
                ));
        datos.put("gastosPorUsuario", gastosPorUsuario);
        
        double promedioGasto = gastosPorUsuario.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        datos.put("promedioGasto", promedioGasto);
        
        reporte.setDatos(datos);
        return reporte;
    }
}

