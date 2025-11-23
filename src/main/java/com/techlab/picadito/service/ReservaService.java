package com.techlab.picadito.service;

import com.techlab.picadito.dto.ReservaDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.*;
import com.techlab.picadito.repository.ReservaRepository;
import com.techlab.picadito.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);
    
    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;
    private final PartidoService partidoService;
    private final PartidosSeleccionadosService partidosSeleccionadosService;
    private final ParticipanteService participanteService;
    private final MapperUtil mapperUtil;
    
    public List<ReservaDTO> obtenerTodos() {
        return reservaRepository.findAll().stream()
                .map(mapperUtil::toReservaDTO)
                .collect(Collectors.toList());
    }
    
    public List<ReservaDTO> obtenerPorUsuario(Long usuarioId) {
        return reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(mapperUtil::toReservaDTO)
                .collect(Collectors.toList());
    }
    
    public ReservaDTO obtenerPorId(Long id) {
        Objects.requireNonNull(id, "El ID de la reserva no puede ser null");
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + id));
        return mapperUtil.toReservaDTO(reserva);
    }
    
    @Transactional
    public ReservaDTO crearDesdePartidosSeleccionados(Long usuarioId) {
        com.techlab.picadito.dto.PartidosSeleccionadosDTO partidosSeleccionadosDTO = partidosSeleccionadosService.obtenerPartidosSeleccionadosPorUsuario(usuarioId);
        
        if (partidosSeleccionadosDTO.getItems().isEmpty()) {
            throw new BusinessException("No hay partidos seleccionados");
        }
        
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuarioService.obtenerUsuarioEntity(usuarioId));
        reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
        
        // Inscribir participantes en cada partido seleccionado
        for (com.techlab.picadito.dto.LineaPartidoSeleccionadoDTO item : partidosSeleccionadosDTO.getItems()) {
            Long partidoId = Objects.requireNonNull(item.getPartidoId(), "El ID del partido no puede ser null");
            Partido partido = partidoService.obtenerPartidoEntity(partidoId);
            
            // Validar disponibilidad
            if (partido.getEstado() != EstadoPartido.DISPONIBLE) {
                throw new BusinessException("El partido '" + partido.getTitulo() + "' no está disponible");
            }
            
            // Validar capacidad
            int capacidadDisponible = partido.getMaxJugadores() - partido.getCantidadParticipantes();
            if (item.getCantidad() > capacidadDisponible) {
                throw new BusinessException("No hay suficiente capacidad disponible en el partido '" + partido.getTitulo() + "'. Capacidad disponible: " + capacidadDisponible);
            }
            
            // Validar que el partido tenga precio (opcional pero recomendado para calcular totales)
            if (partido.getPrecio() == null) {
                logger.warn("El partido '{}' no tiene precio definido. El total de la reserva puede ser 0", partido.getTitulo());
            }
            
            LineaReserva linea = new LineaReserva();
            linea.setReserva(reserva);
            linea.setPartido(partido);
            linea.setCantidad(item.getCantidad());
            
            reserva.getLineasReserva().add(linea);
        }
        
        reserva = reservaRepository.save(reserva);
        
        // Inscribir participantes (simplificado: usar el nombre del usuario)
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);
        for (LineaReserva linea : reserva.getLineasReserva()) {
            for (int i = 0; i < linea.getCantidad(); i++) {
                com.techlab.picadito.dto.ParticipanteDTO participanteDTO = new com.techlab.picadito.dto.ParticipanteDTO();
                participanteDTO.setNombre(usuario.getNombre() + " " + (i + 1));
                try {
                    Long partidoId = Objects.requireNonNull(linea.getPartido().getId(), "El ID del partido no puede ser null");
                    participanteService.inscribirseAPartido(partidoId, participanteDTO);
                } catch (BusinessException e) {
                    // Si falla la inscripción, cancelar la reserva
                    reserva.setEstado(Reserva.EstadoReserva.CANCELADO);
                    reservaRepository.save(reserva);
                    throw new BusinessException("Error al inscribir participantes: " + e.getMessage());
                }
            }
        }
        
        // Confirmar la reserva
        reserva.setEstado(Reserva.EstadoReserva.CONFIRMADO);
        reserva = reservaRepository.save(reserva);
        
        // Actualizar estado automáticamente si algún partido está próximo
        actualizarEstadoAutomatico(reserva);
        
        partidosSeleccionadosService.vaciarPartidosSeleccionados(usuarioId);
        
        return mapperUtil.toReservaDTO(reserva);
    }
    
    @Transactional
    public ReservaDTO actualizarEstado(Long id, Reserva.EstadoReserva nuevoEstado) {
        Objects.requireNonNull(id, "El ID de la reserva no puede ser null");
        Objects.requireNonNull(nuevoEstado, "El nuevo estado no puede ser null");
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + id));
        
        // Validar transición de estado
        validarTransicionEstado(reserva.getEstado(), nuevoEstado);
        
        reserva.setEstado(nuevoEstado);
        reserva = reservaRepository.save(reserva);
        
        return mapperUtil.toReservaDTO(reserva);
    }
    
    /**
     * Valida que la transición de estado sea válida según las reglas de negocio
     */
    private void validarTransicionEstado(Reserva.EstadoReserva estadoActual, Reserva.EstadoReserva nuevoEstado) {
        // Estados terminales no pueden cambiar
        if (estadoActual == Reserva.EstadoReserva.FINALIZADO) {
            throw new BusinessException("No se puede cambiar el estado de una reserva FINALIZADA");
        }
        
        if (estadoActual == Reserva.EstadoReserva.CANCELADO && nuevoEstado != Reserva.EstadoReserva.CANCELADO) {
            throw new BusinessException("No se puede cambiar el estado de una reserva CANCELADA");
        }
        
        // Validar transiciones válidas
        switch (estadoActual) {
            case PENDIENTE:
                if (nuevoEstado != Reserva.EstadoReserva.CONFIRMADO && nuevoEstado != Reserva.EstadoReserva.CANCELADO) {
                    throw new BusinessException("Una reserva PENDIENTE solo puede pasar a CONFIRMADO o CANCELADO");
                }
                break;
            case CONFIRMADO:
                if (nuevoEstado != Reserva.EstadoReserva.EN_PROCESO && nuevoEstado != Reserva.EstadoReserva.CANCELADO) {
                    throw new BusinessException("Una reserva CONFIRMADA solo puede pasar a EN_PROCESO o CANCELADO");
                }
                break;
            case EN_PROCESO:
                if (nuevoEstado != Reserva.EstadoReserva.FINALIZADO && nuevoEstado != Reserva.EstadoReserva.CANCELADO) {
                    throw new BusinessException("Una reserva EN_PROCESO solo puede pasar a FINALIZADO o CANCELADO");
                }
                break;
            case FINALIZADO:
                throw new BusinessException("Una reserva FINALIZADA no puede cambiar de estado");
            case CANCELADO:
                throw new BusinessException("Una reserva CANCELADA no puede cambiar de estado");
        }
    }
    
    /**
     * Actualiza el estado de una reserva específica basándose en las fechas de sus partidos
     */
    private void actualizarEstadoAutomatico(Reserva reserva) {
        if (reserva.getEstado() == Reserva.EstadoReserva.FINALIZADO || 
            reserva.getEstado() == Reserva.EstadoReserva.CANCELADO) {
            return; // Estados terminales, no actualizar
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime proximas24Horas = ahora.plusHours(24);
        
        boolean todosFinalizados = true;
        boolean algunoProximo = false;
        
        for (LineaReserva linea : reserva.getLineasReserva()) {
            Partido partido = linea.getPartido();
            
            // Verificar si algún partido está próximo (menos de 24 horas)
            if (partido.getFechaHora().isAfter(ahora) && 
                partido.getFechaHora().isBefore(proximas24Horas)) {
                algunoProximo = true;
            }
            
            // Verificar si todos los partidos están finalizados
            if (partido.getEstado() != EstadoPartido.FINALIZADO) {
                todosFinalizados = false;
            }
        }
        
        // Actualizar estado según las condiciones
        if (todosFinalizados && reserva.getEstado() != Reserva.EstadoReserva.FINALIZADO) {
            reserva.setEstado(Reserva.EstadoReserva.FINALIZADO);
            reservaRepository.save(reserva);
        } else if (algunoProximo && reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADO) {
            reserva.setEstado(Reserva.EstadoReserva.EN_PROCESO);
            reservaRepository.save(reserva);
        }
    }
    
    /**
     * Actualiza automáticamente los estados de todas las reservas basándose en las fechas de los partidos
     */
    @Transactional
    public void actualizarEstadosAutomaticamente() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime proximas24Horas = ahora.plusHours(24);
        
        List<Reserva> reservas = reservaRepository.findAll();
        
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() == Reserva.EstadoReserva.FINALIZADO || 
                reserva.getEstado() == Reserva.EstadoReserva.CANCELADO) {
                continue; // Estados terminales, no actualizar
            }
            
            boolean todosFinalizados = true;
            boolean algunoProximo = false;
            
            for (LineaReserva linea : reserva.getLineasReserva()) {
                Partido partido = linea.getPartido();
                
                // Verificar si algún partido está próximo (menos de 24 horas)
                if (partido.getFechaHora().isAfter(ahora) && 
                    partido.getFechaHora().isBefore(proximas24Horas)) {
                    algunoProximo = true;
                }
                
                // Verificar si todos los partidos están finalizados
                if (partido.getEstado() != EstadoPartido.FINALIZADO) {
                    todosFinalizados = false;
                }
            }
            
            // Actualizar estado según las condiciones
            if (todosFinalizados && reserva.getEstado() != Reserva.EstadoReserva.FINALIZADO) {
                reserva.setEstado(Reserva.EstadoReserva.FINALIZADO);
                reservaRepository.save(reserva);
            } else if (algunoProximo && reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADO) {
                reserva.setEstado(Reserva.EstadoReserva.EN_PROCESO);
                reservaRepository.save(reserva);
            }
        }
    }
    
    @Transactional
    public void cancelar(Long id) {
        Objects.requireNonNull(id, "El ID de la reserva no puede ser null");
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + id));
        
        // No se puede cancelar una reserva finalizada
        if (reserva.getEstado() == Reserva.EstadoReserva.FINALIZADO) {
            throw new BusinessException("No se puede cancelar una reserva FINALIZADA");
        }
        
        reserva.setEstado(Reserva.EstadoReserva.CANCELADO);
        reservaRepository.save(reserva);
    }
    
    /**
     * Calcula el total gastado por un usuario en todas sus reservas confirmadas o finalizadas
     * @param usuarioId ID del usuario
     * @return Total gastado
     */
    public Double calcularTotalGastadoPorUsuario(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser null");
        
        List<Reserva> reservas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        
        return reservas.stream()
                .filter(reserva -> reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADO || 
                                 reserva.getEstado() == Reserva.EstadoReserva.FINALIZADO)
                .mapToDouble(Reserva::calcularTotal)
                .sum();
    }
}

