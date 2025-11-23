package com.techlab.picadito.service;

import com.techlab.picadito.dto.ParticipanteDTO;
import com.techlab.picadito.dto.ParticipanteResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Participante;
import com.techlab.picadito.repository.ParticipanteRepository;
import com.techlab.picadito.repository.PartidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParticipanteService {

    private static final Logger logger = LoggerFactory.getLogger(ParticipanteService.class);

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private PartidoService partidoService;

    public ParticipanteResponseDTO inscribirseAPartido(@NonNull Long partidoId, ParticipanteDTO participanteDTO) {
        logger.info("Inscribiendo participante {} al partido {}", participanteDTO.getNombre(), partidoId);
        
        // Recargar el partido con bloqueo pesimista para evitar race conditions
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + partidoId));

        // Validar que el partido esté disponible
        if (partido.getEstado() != EstadoPartido.DISPONIBLE) {
            throw new BusinessException("No se puede inscribir a un partido que no está disponible. Estado actual: " + partido.getEstado());
        }

        // Validar que no esté completo (después de bloqueo)
        if (partido.estaCompleto()) {
            throw new BusinessException("El partido ya está completo. Máximo de jugadores: " + partido.getMaxJugadores());
        }

        // Validar que el nombre no esté ya inscrito
        if (participanteRepository.existsByPartidoAndNombre(partido, participanteDTO.getNombre())) {
            throw new BusinessException("Ya existe un participante con el nombre '" + participanteDTO.getNombre() + "' en este partido");
        }

        // Crear participante
        Participante participante = new Participante();
        participante.setNombre(participanteDTO.getNombre());
        participante.setApodo(participanteDTO.getApodo());
        participante.setPosicion(participanteDTO.getPosicion());
        participante.setNivel(participanteDTO.getNivel());
        participante.setPartido(partido);

        participante = participanteRepository.save(participante);
        
        // Recargar el partido para obtener la lista actualizada de participantes
        partido = partidoRepository.findById(partidoId).orElse(partido);
        
        // Actualizar estado del partido si está completo
        partidoService.actualizarEstadoSegunParticipantes(partido);

        logger.info("Participante inscrito exitosamente con id: {}", participante.getId());
        return convertirADTO(participante);
    }

    public List<ParticipanteResponseDTO> obtenerParticipantesPorPartido(@NonNull Long partidoId) {
        logger.debug("Obteniendo participantes del partido {}", partidoId);
        if (!partidoRepository.existsById(partidoId)) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + partidoId);
        }

        return participanteRepository.findByPartidoId(partidoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public void desinscribirseDePartido(@NonNull Long partidoId, @NonNull Long participanteId) {
        logger.info("Desinscribiendo participante {} del partido {}", participanteId, partidoId);
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + partidoId));

        Participante participante = participanteRepository.findById(participanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Participante no encontrado con id: " + participanteId));

        if (!participante.getPartido().getId().equals(partidoId)) {
            throw new BusinessException("El participante no pertenece a este partido");
        }

        participanteRepository.delete(participante);
        partido.getParticipantes().remove(participante);

        // Actualizar estado del partido si ya no está completo
        partidoService.actualizarEstadoSegunParticipantes(partido);
        logger.info("Participante desinscrito exitosamente");
    }

    public boolean existeParticipanteEnPartido(Long partidoId, String nombre) {
        Objects.requireNonNull(partidoId, "El ID del partido no puede ser null");
        Partido partido = partidoRepository.findById(partidoId)
                .orElse(null);
        if (partido == null) {
            return false;
        }
        return participanteRepository.existsByPartidoAndNombre(partido, nombre);
    }

    public Participante obtenerParticipanteEntity(Long participanteId) {
        Objects.requireNonNull(participanteId, "El ID del participante no puede ser null");
        return participanteRepository.findById(participanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Participante no encontrado con id: " + participanteId));
    }

    private ParticipanteResponseDTO convertirADTO(Participante participante) {
        ParticipanteResponseDTO dto = new ParticipanteResponseDTO();
        dto.setId(participante.getId());
        dto.setNombre(participante.getNombre());
        dto.setApodo(participante.getApodo());
        dto.setPosicion(participante.getPosicion());
        dto.setNivel(participante.getNivel());
        dto.setFechaInscripcion(participante.getFechaInscripcion());
        return dto;
    }
}

