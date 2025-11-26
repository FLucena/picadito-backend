package com.techlab.picadito.service;

import com.techlab.picadito.dto.EstadisticasDTO;
import com.techlab.picadito.model.*;
import com.techlab.picadito.repository.*;
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
public class EstadisticasService {

    private static final Logger logger = LoggerFactory.getLogger(EstadisticasService.class);

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SedeRepository sedeRepository;

    public EstadisticasDTO obtenerEstadisticasGenerales() {
        logger.info("Generando estadísticas generales");
        
        EstadisticasDTO estadisticas = new EstadisticasDTO();
        
        estadisticas.setTotalPartidos(partidoRepository.count());
        estadisticas.setTotalReservas(reservaRepository.count());
        estadisticas.setTotalUsuarios(usuarioRepository.count());
        
        // Calcular ingresos totales
        Double ingresosTotales = reservaRepository.findAll().stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.CONFIRMADO || 
                           r.getEstado() == Reserva.EstadoReserva.FINALIZADO)
                .mapToDouble(Reserva::calcularTotal)
                .sum();
        estadisticas.setIngresosTotales(ingresosTotales);
        
        // Partidos más populares
        estadisticas.setPartidosPopulares(obtenerPartidosPopulares());
        
        // Usuarios más activos
        estadisticas.setUsuariosActivos(obtenerUsuariosActivos());
        
        // Sedes más utilizadas
        estadisticas.setSedesUtilizadas(obtenerSedesUtilizadas());
        
        // Partidos por categoría
        estadisticas.setPartidosPorCategoria(obtenerPartidosPorCategoria());
        
        // Tasa de ocupación promedio
        estadisticas.setTasaOcupacionPromedio(calcularTasaOcupacionPromedio());
        
        return estadisticas;
    }

    public EstadisticasDTO obtenerEstadisticasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        logger.info("Generando estadísticas para el período {} - {}", fechaInicio, fechaFin);
        
        EstadisticasDTO estadisticas = new EstadisticasDTO();
        
        // Filtrar reservas por período
        List<Reserva> reservasPeriodo = reservaRepository.findAll().stream()
                .filter(r -> r.getFechaCreacion().isAfter(fechaInicio) && 
                           r.getFechaCreacion().isBefore(fechaFin))
                .collect(Collectors.toList());
        
        estadisticas.setTotalReservas((long) reservasPeriodo.size());
        
        Double ingresosPeriodo = reservasPeriodo.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.CONFIRMADO || 
                           r.getEstado() == Reserva.EstadoReserva.FINALIZADO)
                .mapToDouble(Reserva::calcularTotal)
                .sum();
        estadisticas.setIngresosPorPeriodo(ingresosPeriodo);
        
        // Filtrar partidos por período
        List<Partido> partidosPeriodo = partidoRepository.findAll().stream()
                .filter(p -> p.getFechaCreacion().isAfter(fechaInicio) && 
                           p.getFechaCreacion().isBefore(fechaFin))
                .collect(Collectors.toList());
        
        estadisticas.setTotalPartidos((long) partidosPeriodo.size());
        
        return estadisticas;
    }

    private List<EstadisticasDTO.PartidoPopularDTO> obtenerPartidosPopulares() {
        return partidoRepository.findAll().stream()
                .filter(p -> p.getCantidadParticipantes() > 0)
                .map(p -> {
                    EstadisticasDTO.PartidoPopularDTO dto = new EstadisticasDTO.PartidoPopularDTO();
                    dto.setPartidoId(p.getId());
                    dto.setTitulo(p.getTitulo());
                    dto.setCantidadParticipantes(p.getCantidadParticipantes());
                    dto.setMaxJugadores(p.getMaxJugadores());
                    double porcentaje = (double) p.getCantidadParticipantes() / p.getMaxJugadores() * 100;
                    dto.setPorcentajeOcupacion(porcentaje);
                    return dto;
                })
                .sorted((a, b) -> b.getCantidadParticipantes().compareTo(a.getCantidadParticipantes()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<EstadisticasDTO.UsuarioActivoDTO> obtenerUsuariosActivos() {
        Map<Long, Long> reservasPorUsuario = reservaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getUsuario().getId(),
                        Collectors.counting()
                ));
        
        Map<Long, Double> gastosPorUsuario = reservaRepository.findAll().stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.CONFIRMADO || 
                           r.getEstado() == Reserva.EstadoReserva.FINALIZADO)
                .collect(Collectors.groupingBy(
                        r -> r.getUsuario().getId(),
                        Collectors.summingDouble(Reserva::calcularTotal)
                ));
        
        return usuarioRepository.findAll().stream()
                .map(u -> {
                    EstadisticasDTO.UsuarioActivoDTO dto = new EstadisticasDTO.UsuarioActivoDTO();
                    dto.setUsuarioId(u.getId());
                    dto.setNombre(u.getNombre());
                    dto.setCantidadReservas(reservasPorUsuario.getOrDefault(u.getId(), 0L));
                    dto.setTotalGastado(gastosPorUsuario.getOrDefault(u.getId(), 0.0));
                    return dto;
                })
                .filter(u -> u.getCantidadReservas() > 0)
                .sorted((a, b) -> b.getCantidadReservas().compareTo(a.getCantidadReservas()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<EstadisticasDTO.SedeUtilizadaDTO> obtenerSedesUtilizadas() {
        Map<Long, Long> partidosPorSede = partidoRepository.findAll().stream()
                .filter(p -> p.getSede() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getSede().getId(),
                        Collectors.counting()
                ));
        
        return sedeRepository.findAll().stream()
                .filter(s -> partidosPorSede.containsKey(s.getId()))
                .map(s -> {
                    EstadisticasDTO.SedeUtilizadaDTO dto = new EstadisticasDTO.SedeUtilizadaDTO();
                    dto.setSedeId(s.getId());
                    dto.setNombre(s.getNombre() != null ? s.getNombre() : s.getDireccion());
                    dto.setCantidadPartidos(partidosPorSede.get(s.getId()));
                    return dto;
                })
                .sorted((a, b) -> b.getCantidadPartidos().compareTo(a.getCantidadPartidos()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private Map<String, Long> obtenerPartidosPorCategoria() {
        // Contar todas las relaciones partido-categoría (un partido puede tener múltiples categorías)
        return partidoRepository.findAll().stream()
                .filter(p -> p.getCategorias() != null && !p.getCategorias().isEmpty())
                .flatMap(p -> p.getCategorias().stream()
                        .map(c -> new AbstractMap.SimpleEntry<>(c.getNombre(), p.getId())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.counting()
                ));
    }

    private Double calcularTasaOcupacionPromedio() {
        List<Partido> partidos = partidoRepository.findAll();
        if (partidos.isEmpty()) {
            return 0.0;
        }
        
        double sumaPorcentajes = partidos.stream()
                .filter(p -> p.getMaxJugadores() > 0)
                .mapToDouble(p -> (double) p.getCantidadParticipantes() / p.getMaxJugadores() * 100)
                .sum();
        
        long totalPartidos = partidos.stream()
                .filter(p -> p.getMaxJugadores() > 0)
                .count();
        
        return totalPartidos > 0 ? sumaPorcentajes / totalPartidos : 0.0;
    }
}

