package com.techlab.picadito.reserva;

import com.techlab.picadito.dto.ReservaDTO;
import com.techlab.picadito.dto.ReservasResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.*;
import com.techlab.picadito.partido.PartidoService;
import com.techlab.picadito.participante.ParticipanteService;
import com.techlab.picadito.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);
    
    private final ReservaRepository reservaRepository;
    private final com.techlab.picadito.usuario.UsuarioService usuarioService;
    private final PartidoService partidoService;
    private final com.techlab.picadito.partidosseleccionados.PartidosSeleccionadosService partidosSeleccionadosService;
    private final ParticipanteService participanteService;
    private final MapperUtil mapperUtil;
    private final com.techlab.picadito.alerta.AlertaService alertaService;
    
    private static final Map<Reserva.EstadoReserva, Set<Reserva.EstadoReserva>> TRANSICIONES_VALIDAS = new HashMap<>();
    
    static {
        TRANSICIONES_VALIDAS.put(Reserva.EstadoReserva.PENDIENTE, 
            Set.of(Reserva.EstadoReserva.CONFIRMADO, Reserva.EstadoReserva.CANCELADO));
        TRANSICIONES_VALIDAS.put(Reserva.EstadoReserva.CONFIRMADO, 
            Set.of(Reserva.EstadoReserva.EN_PROCESO, Reserva.EstadoReserva.CANCELADO));
        TRANSICIONES_VALIDAS.put(Reserva.EstadoReserva.EN_PROCESO, 
            Set.of(Reserva.EstadoReserva.FINALIZADO, Reserva.EstadoReserva.CANCELADO));
    }
    
    public ReservasResponseDTO obtenerTodos() {
        logger.debug("Obteniendo todas las reservas ordenadas por fecha de creación");
        List<ReservaDTO> reservas = reservaRepository.findAllByOrderByFechaCreacionDesc().stream()
                .map(mapperUtil::toReservaDTO)
                .collect(Collectors.toList());
        return new ReservasResponseDTO(reservas);
    }
    
    public ReservasResponseDTO obtenerPorUsuario(Long usuarioId) {
        List<ReservaDTO> reservas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(mapperUtil::toReservaDTO)
                .collect(Collectors.toList());
        return new ReservasResponseDTO(reservas);
    }
    
    public ReservaDTO obtenerPorId(Long id) {
        Objects.requireNonNull(id, "El ID de la reserva no puede ser null");
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + id));
        return mapperUtil.toReservaDTO(reserva);
    }
    
    @Transactional
    @SuppressWarnings("null")
    public ReservaDTO crearDesdePartidosSeleccionados(Long usuarioId) {
        com.techlab.picadito.dto.PartidosSeleccionadosDTO partidosSeleccionadosDTO = partidosSeleccionadosService.obtenerPartidosSeleccionadosPorUsuario(usuarioId);
        
        if (partidosSeleccionadosDTO.getItems().isEmpty()) {
            throw new BusinessException("No hay partidos seleccionados");
        }
        
        Reserva reserva = crearReservaInicial(usuarioId);
        validarYCrearLineasReserva(reserva, partidosSeleccionadosDTO);
        reserva = reservaRepository.save(reserva);
        
        inscribirParticipantesEnReserva(reserva, usuarioId);
        
        // Confirmar la reserva
        reserva.setEstado(Reserva.EstadoReserva.CONFIRMADO);
        reserva = reservaRepository.save(reserva);
        
        // Generar alertas de confirmación para cada partido
        for (LineaReserva linea : reserva.getLineasReserva()) {
            alertaService.crearAlertaReservaConfirmada(usuarioId, linea.getPartido().getTitulo());
        }
        
        // Actualizar estado automáticamente si algún partido está próximo
        actualizarEstadoAutomatico(reserva);
        
        partidosSeleccionadosService.vaciarPartidosSeleccionados(usuarioId);
        
        return mapperUtil.toReservaDTO(reserva);
    }
    
    private Reserva crearReservaInicial(Long usuarioId) {
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuarioService.obtenerUsuarioEntity(usuarioId));
        reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
        return reserva;
    }
    
    private void validarYCrearLineasReserva(Reserva reserva, com.techlab.picadito.dto.PartidosSeleccionadosDTO partidosSeleccionadosDTO) {
        for (com.techlab.picadito.dto.LineaPartidoSeleccionadoDTO item : partidosSeleccionadosDTO.getItems()) {
            Long partidoId = Objects.requireNonNull(item.getPartidoId(), "El ID del partido no puede ser null");
            Partido partido = partidoService.obtenerPartidoEntity(partidoId);
            
            validarPartidoParaReserva(partido, item.getCantidad());
            
            LineaReserva linea = new LineaReserva();
            linea.setReserva(reserva);
            linea.setPartido(partido);
            linea.setCantidad(item.getCantidad());
            
            reserva.getLineasReserva().add(linea);
        }
    }
    
    private void validarPartidoParaReserva(Partido partido, Integer cantidad) {
        // Validar disponibilidad
        if (partido.getEstado() != EstadoPartido.DISPONIBLE) {
            throw new BusinessException("El partido '" + partido.getTitulo() + "' no está disponible");
        }
        
        // Validar capacidad
        int capacidadDisponible = partido.getMaxJugadores() - partido.getCantidadParticipantes();
        if (cantidad > capacidadDisponible) {
            throw new BusinessException("No hay suficiente capacidad disponible en el partido '" + partido.getTitulo() + "'. Capacidad disponible: " + capacidadDisponible);
        }
        
        // Validar que el partido tenga precio (opcional pero recomendado para calcular totales)
        if (partido.getPrecio() == null) {
            logger.warn("El partido '{}' no tiene precio definido. El total de la reserva puede ser 0", partido.getTitulo());
        }
    }
    
    private void inscribirParticipantesEnReserva(Reserva reserva, Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);
        for (LineaReserva linea : reserva.getLineasReserva()) {
            inscribirParticipantesEnLinea(linea, usuario, reserva);
        }
    }
    
    private void inscribirParticipantesEnLinea(LineaReserva linea, Usuario usuario, Reserva reserva) {
        for (int i = 0; i < linea.getCantidad(); i++) {
            com.techlab.picadito.dto.ParticipanteDTO participanteDTO = crearParticipanteDTO(usuario, i);
            inscribirParticipanteConManejoErrores(linea, participanteDTO, reserva);
        }
    }
    
    private com.techlab.picadito.dto.ParticipanteDTO crearParticipanteDTO(Usuario usuario, int indice) {
        com.techlab.picadito.dto.ParticipanteDTO participanteDTO = new com.techlab.picadito.dto.ParticipanteDTO();
        participanteDTO.setNombre(usuario.getNombre() + " " + (indice + 1));
        return participanteDTO;
    }
    
    private void inscribirParticipanteConManejoErrores(LineaReserva linea, com.techlab.picadito.dto.ParticipanteDTO participanteDTO, Reserva reserva) {
        try {
            Long partidoId = Objects.requireNonNull(linea.getPartido().getId(), "El ID del partido no puede ser null");
            participanteService.inscribirseAPartido(partidoId, participanteDTO);
        } catch (BusinessException e) {
            cancelarReservaPorError(reserva, e);
            throw new BusinessException("Error al inscribir participantes: " + e.getMessage());
        }
    }
    
    private void cancelarReservaPorError(Reserva reserva, BusinessException e) {
        reserva.setEstado(Reserva.EstadoReserva.CANCELADO);
        reservaRepository.save(reserva);
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
        
        // Validar transiciones válidas usando el Map
        Set<Reserva.EstadoReserva> transicionesValidas = TRANSICIONES_VALIDAS.get(estadoActual);
        if (transicionesValidas == null || !transicionesValidas.contains(nuevoEstado)) {
            throw new BusinessException("Transición inválida: una reserva " + estadoActual.name() + 
                " no puede pasar a " + nuevoEstado.name());
        }
    }
    
    /**
     * Actualiza el estado de una reserva específica basándose en las fechas de sus partidos
     */
    private void actualizarEstadoAutomatico(Reserva reserva) {
        if (esEstadoTerminal(reserva.getEstado())) {
            return; // Estados terminales, no actualizar
        }
        
        EstadoEvaluacion evaluacion = evaluarEstadoReserva(reserva);
        aplicarActualizacionEstado(reserva, evaluacion);
    }
    
    private boolean esEstadoTerminal(Reserva.EstadoReserva estado) {
        return estado == Reserva.EstadoReserva.FINALIZADO || 
               estado == Reserva.EstadoReserva.CANCELADO;
    }
    
    private EstadoEvaluacion evaluarEstadoReserva(Reserva reserva) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime proximas24Horas = ahora.plusHours(24);
        
        boolean todosFinalizados = true;
        boolean algunoProximo = false;
        
        for (LineaReserva linea : reserva.getLineasReserva()) {
            Partido partido = linea.getPartido();
            
            if (esPartidoProximo(partido, ahora, proximas24Horas)) {
                algunoProximo = true;
            }
            
            if (partido.getEstado() != EstadoPartido.FINALIZADO) {
                todosFinalizados = false;
            }
        }
        
        return new EstadoEvaluacion(todosFinalizados, algunoProximo);
    }
    
    private boolean esPartidoProximo(Partido partido, LocalDateTime ahora, LocalDateTime proximas24Horas) {
        return partido.getFechaHora().isAfter(ahora) && 
               partido.getFechaHora().isBefore(proximas24Horas);
    }
    
    private void aplicarActualizacionEstado(Reserva reserva, EstadoEvaluacion evaluacion) {
        if (evaluacion.todosFinalizados && reserva.getEstado() != Reserva.EstadoReserva.FINALIZADO) {
            reserva.setEstado(Reserva.EstadoReserva.FINALIZADO);
            reservaRepository.save(reserva);
        } else if (evaluacion.algunoProximo && reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADO) {
            reserva.setEstado(Reserva.EstadoReserva.EN_PROCESO);
            reservaRepository.save(reserva);
        }
    }
    
    private static class EstadoEvaluacion {
        final boolean todosFinalizados;
        final boolean algunoProximo;
        
        EstadoEvaluacion(boolean todosFinalizados, boolean algunoProximo) {
            this.todosFinalizados = todosFinalizados;
            this.algunoProximo = algunoProximo;
        }
    }
    
    /**
     * Actualiza automáticamente los estados de todas las reservas basándose en las fechas de los partidos
     */
    @Transactional
    public void actualizarEstadosAutomaticamente() {
        logger.debug("Actualizando estados automáticamente de todas las reservas");
        // Obtener solo reservas que no están en estados terminales
        List<Reserva.EstadoReserva> estadosNoTerminales = List.of(
            Reserva.EstadoReserva.PENDIENTE,
            Reserva.EstadoReserva.CONFIRMADO,
            Reserva.EstadoReserva.EN_PROCESO
        );
        List<Reserva> reservas = reservaRepository.findByEstadoInOrderByFechaCreacionDesc(estadosNoTerminales);
        
        for (Reserva reserva : reservas) {
            actualizarEstadoAutomatico(reserva);
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

