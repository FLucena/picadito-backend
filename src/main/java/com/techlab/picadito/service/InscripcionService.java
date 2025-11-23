package com.techlab.picadito.service;

import com.techlab.picadito.dto.InscripcionConfirmadaResponseDTO;
import com.techlab.picadito.dto.LineaInscripcionDTO;
import com.techlab.picadito.dto.ParticipanteDTO;
import com.techlab.picadito.dto.ParticipanteResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.EstadoInscripcion;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.PartidosGuardados;
import com.techlab.picadito.model.InscripcionConfirmada;
import com.techlab.picadito.model.LineaPartidoGuardado;
import com.techlab.picadito.model.LineaInscripcion;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Participante;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.repository.PartidosGuardadosRepository;
import com.techlab.picadito.repository.InscripcionRepository;
import com.techlab.picadito.repository.PartidoRepository;
import com.techlab.picadito.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class InscripcionService {

    private static final Logger logger = LoggerFactory.getLogger(InscripcionService.class);

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private PartidosGuardadosRepository partidosGuardadosRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ParticipanteService participanteService;

    public InscripcionConfirmadaResponseDTO confirmarInscripcionesDesdePartidosGuardados(Long usuarioId, ParticipanteDTO participanteDTO) {
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser null");
        Objects.requireNonNull(participanteDTO, "El DTO del participante no puede ser null");
        logger.info("Confirmando inscripciones desde partidos guardados para usuario: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        PartidosGuardados partidosGuardados = partidosGuardadosRepository.findByUsuario(usuario)
                .orElseThrow(() -> new BusinessException("No tienes partidos guardados para confirmar"));

        if (partidosGuardados.getPartidos().isEmpty()) {
            throw new BusinessException("Tus partidos guardados están vacíos. Agrega partidos antes de confirmar");
        }

        // Validar todos los partidos antes de crear inscripciones
        List<String> errores = new ArrayList<>();
        for (LineaPartidoGuardado linea : partidosGuardados.getPartidos()) {
            final Long partidoId = Objects.requireNonNull(linea.getPartido().getId(), "El ID del partido no puede ser null");
            
            // Recargar partido para obtener estado actual
            Partido partido = partidoRepository.findById(partidoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado: " + partidoId));

            if (partido.getEstado() != EstadoPartido.DISPONIBLE) {
                errores.add(String.format("El partido '%s' ya no está disponible (Estado: %s)", 
                    partido.getTitulo(), partido.getEstado()));
            }

            if (partido.estaCompleto()) {
                errores.add(String.format("El partido '%s' ya está completo (%d/%d jugadores)", 
                    partido.getTitulo(), partido.getCantidadParticipantes(), partido.getMaxJugadores()));
            }

            // Verificar si el participante ya está inscrito
            if (participanteService.existeParticipanteEnPartido(partido.getId(), participanteDTO.getNombre())) {
                errores.add(String.format("Ya estás inscrito en el partido '%s'", partido.getTitulo()));
            }
        }

        if (!errores.isEmpty()) {
            throw new BusinessException("No se pueden confirmar las inscripciones:\n" + String.join("\n", errores));
        }

        // Crear la inscripción confirmada
        InscripcionConfirmada inscripcion = new InscripcionConfirmada();
        inscripcion.setUsuario(usuario);
        inscripcion.setEstado(EstadoInscripcion.PENDIENTE);
        inscripcion.setTotalPartidos(partidosGuardados.getPartidos().size());

        List<LineaInscripcion> lineasInscripcion = new ArrayList<>();

        // Crear participantes e inscripciones para cada partido
        for (LineaPartidoGuardado lineaPartidoGuardado : partidosGuardados.getPartidos()) {
            final Long partidoId = Objects.requireNonNull(lineaPartidoGuardado.getPartido().getId(), "El ID del partido no puede ser null");
            Partido partido = partidoRepository.findById(partidoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado: " + partidoId));

            // Inscribir al participante en el partido usando el servicio
            ParticipanteResponseDTO participanteResponse = participanteService.inscribirseAPartido(
                partidoId, 
                participanteDTO
            );
            
            // Obtener el participante entity
            Long participanteId = Objects.requireNonNull(participanteResponse.getId(), "El ID del participante no puede ser null");
            Participante participante = participanteService.obtenerParticipanteEntity(participanteId);

            // Crear línea de inscripción
            LineaInscripcion lineaInscripcion = new LineaInscripcion();
            lineaInscripcion.setInscripcion(inscripcion);
            lineaInscripcion.setPartido(partido);
            lineaInscripcion.setParticipante(participante);
            lineaInscripcion.setEstado(EstadoInscripcion.CONFIRMADA);

            lineasInscripcion.add(lineaInscripcion);
        }

        inscripcion.setLineasInscripcion(lineasInscripcion);
        inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);

        // Guardar la inscripción
        inscripcion = inscripcionRepository.save(inscripcion);

        // Vaciar los partidos guardados después de confirmar
        partidosGuardados.getPartidos().clear();
        partidosGuardadosRepository.save(partidosGuardados);

        logger.info("Inscripciones confirmadas exitosamente. Total: {}", inscripcion.getTotalPartidos());
        return convertirADTO(inscripcion);
    }


    public List<InscripcionConfirmadaResponseDTO> obtenerInscripcionesPorUsuario(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser null");
        logger.debug("Obteniendo inscripciones para usuario: {}", usuarioId);
        
        // No necesitamos obtener el usuario si solo lo usamos para validar existencia
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId);
        }

        return inscripcionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public InscripcionConfirmadaResponseDTO obtenerInscripcionPorId(Long id) {
        Objects.requireNonNull(id, "El ID de la inscripción no puede ser null");
        logger.debug("Obteniendo inscripción con id: {}", id);

        InscripcionConfirmada inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + id));

        return convertirADTO(inscripcion);
    }

    public void cancelarInscripcion(Long inscripcionId) {
        Objects.requireNonNull(inscripcionId, "El ID de la inscripción no puede ser null");
        logger.info("Cancelando inscripción: {}", inscripcionId);

        InscripcionConfirmada inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + inscripcionId));

        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADA) {
            throw new BusinessException("La inscripción ya está cancelada");
        }

        // Desinscribir participantes de cada partido
        for (LineaInscripcion linea : inscripcion.getLineasInscripcion()) {
            if (linea.getEstado() == EstadoInscripcion.CONFIRMADA) {
                // Eliminar participante del partido
                Long partidoId = Objects.requireNonNull(linea.getPartido().getId(), "El ID del partido no puede ser null");
                Long participanteId = Objects.requireNonNull(linea.getParticipante().getId(), "El ID del participante no puede ser null");
                participanteService.desinscribirseDePartido(partidoId, participanteId);
                linea.setEstado(EstadoInscripcion.CANCELADA);
            }
        }

        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        inscripcionRepository.save(inscripcion);

        logger.info("Inscripción {} cancelada exitosamente", inscripcionId);
    }

    private InscripcionConfirmadaResponseDTO convertirADTO(InscripcionConfirmada inscripcion) {
        InscripcionConfirmadaResponseDTO dto = new InscripcionConfirmadaResponseDTO();
        dto.setId(inscripcion.getId());
        dto.setUsuarioId(inscripcion.getUsuario().getId());
        dto.setUsuarioNombre(inscripcion.getUsuario().getNombre());
        dto.setEstado(inscripcion.getEstado());
        dto.setTotalPartidos(inscripcion.getTotalPartidos());
        dto.setFechaCreacion(inscripcion.getFechaCreacion());
        dto.setFechaActualizacion(inscripcion.getFechaActualizacion());

        dto.setLineasInscripcion(inscripcion.getLineasInscripcion().stream()
                .map(this::convertirLineaADTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private LineaInscripcionDTO convertirLineaADTO(LineaInscripcion linea) {
        LineaInscripcionDTO dto = new LineaInscripcionDTO();
        dto.setId(linea.getId());
        dto.setPartidoId(linea.getPartido().getId());
        dto.setPartidoTitulo(linea.getPartido().getTitulo());
        dto.setPartidoUbicacion(linea.getPartido().getUbicacion());
        dto.setPartidoFechaHora(linea.getPartido().getFechaHora().toString());
        dto.setParticipanteId(linea.getParticipante().getId());
        dto.setParticipanteNombre(linea.getParticipante().getNombre());
        dto.setEstado(linea.getEstado());
        return dto;
    }
}

