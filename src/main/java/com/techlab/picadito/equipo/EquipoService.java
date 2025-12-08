package com.techlab.picadito.equipo;

import com.techlab.picadito.dto.EquipoResponseDTO;
import com.techlab.picadito.dto.EquiposResponseDTO;
import com.techlab.picadito.dto.ParticipanteResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Equipo;
import com.techlab.picadito.model.Participante;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Posicion;
import com.techlab.picadito.partido.PartidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipoService {

    private static final Logger logger = LoggerFactory.getLogger(EquipoService.class);

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private PartidoService partidoService;

    public EquiposResponseDTO generarEquiposAutomaticos(@NonNull Long partidoId) {
        logger.info("Generando equipos automáticos para el partido {}", partidoId);
        
        Partido partido = partidoService.obtenerPartidoEntity(partidoId);
        
        if (partido.getParticipantes().size() < 2) {
            throw new BusinessException("Se necesitan al menos 2 participantes para formar equipos");
        }

        // Eliminar equipos existentes si los hay
        List<Equipo> equiposExistentes = equipoRepository.findByPartidoId(partidoId);
        if (equiposExistentes != null && !equiposExistentes.isEmpty()) {
            equipoRepository.deleteAll(equiposExistentes);
        }

        List<Participante> participantes = new ArrayList<>(partido.getParticipantes());
        
        // Dividir en 2 equipos balanceados
        List<List<Participante>> equiposBalanceados = balancearEquipos(participantes);
        
        Equipo equipo1 = Objects.requireNonNull(crearEquipo(partido, "Equipo A", equiposBalanceados.get(0)), "Error al crear equipo1");
        Equipo equipo2 = Objects.requireNonNull(crearEquipo(partido, "Equipo B", equiposBalanceados.get(1)), "Error al crear equipo2");
        
        equipo1 = Objects.requireNonNull(equipoRepository.save(equipo1), "Error al guardar equipo1");
        equipo2 = Objects.requireNonNull(equipoRepository.save(equipo2), "Error al guardar equipo2");
        
        logger.info("Equipos generados exitosamente. Equipo 1: {} participantes, Equipo 2: {} participantes",
                equipo1.getCantidadParticipantes(), equipo2.getCantidadParticipantes());
        
        List<EquipoResponseDTO> equipos = Arrays.asList(convertirADTO(equipo1), convertirADTO(equipo2));
        return new EquiposResponseDTO(equipos);
    }

    public EquiposResponseDTO obtenerEquiposPorPartido(@NonNull Long partidoId) {
        logger.debug("Obteniendo equipos del partido {}", partidoId);
        List<EquipoResponseDTO> equipos = equipoRepository.findByPartidoId(partidoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return new EquiposResponseDTO(equipos);
    }

    public EquipoResponseDTO obtenerPorId(@NonNull Long id) {
        logger.debug("Buscando equipo con id: {}", id);
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + id));
        return convertirADTO(equipo);
    }

    public void eliminarEquipos(@NonNull Long partidoId) {
        logger.info("Eliminando equipos del partido {}", partidoId);
        List<Equipo> equipos = equipoRepository.findByPartidoId(partidoId);
        if (equipos != null && !equipos.isEmpty()) {
            equipoRepository.deleteAll(equipos);
            logger.info("Equipos eliminados exitosamente");
        } else {
            logger.info("No se encontraron equipos para eliminar");
        }
    }

    private List<List<Participante>> balancearEquipos(List<Participante> participantes) {
        // Agrupar por posición
        Map<Posicion, List<Participante>> porPosicion = participantes.stream()
                .filter(p -> p.getPosicion() != null)
                .collect(Collectors.groupingBy(Participante::getPosicion));
        
        List<Participante> sinPosicion = participantes.stream()
                .filter(p -> p.getPosicion() == null)
                .collect(Collectors.toList());
        
        List<Participante> equipo1 = new ArrayList<>();
        List<Participante> equipo2 = new ArrayList<>();
        
        // Distribuir por posición
        for (Map.Entry<Posicion, List<Participante>> entry : porPosicion.entrySet()) {
            List<Participante> posicionList = new ArrayList<>(entry.getValue());
            // Ordenar por nivel (experto primero)
            posicionList.sort((a, b) -> {
                if (a.getNivel() == null && b.getNivel() == null) return 0;
                if (a.getNivel() == null) return 1;
                if (b.getNivel() == null) return -1;
                return b.getNivel().compareTo(a.getNivel());
            });
            
            // Distribuir alternadamente
            for (int i = 0; i < posicionList.size(); i++) {
                if (i % 2 == 0) {
                    equipo1.add(posicionList.get(i));
                } else {
                    equipo2.add(posicionList.get(i));
                }
            }
        }
        
        // Distribuir participantes sin posición
        Collections.shuffle(sinPosicion);
        for (int i = 0; i < sinPosicion.size(); i++) {
            if (i % 2 == 0) {
                equipo1.add(sinPosicion.get(i));
            } else {
                equipo2.add(sinPosicion.get(i));
            }
        }
        
        // Balancear por cantidad si hay diferencia
        while (Math.abs(equipo1.size() - equipo2.size()) > 1) {
            if (equipo1.size() > equipo2.size()) {
                equipo2.add(equipo1.remove(equipo1.size() - 1));
            } else {
                equipo1.add(equipo2.remove(equipo2.size() - 1));
            }
        }
        
        return Arrays.asList(equipo1, equipo2);
    }

    private Equipo crearEquipo(Partido partido, String nombre, List<Participante> participantes) {
        Equipo equipo = new Equipo();
        equipo.setNombre(nombre);
        equipo.setPartido(partido);
        equipo.setParticipantes(participantes);
        return equipo;
    }

    private EquipoResponseDTO convertirADTO(Equipo equipo) {
        EquipoResponseDTO dto = new EquipoResponseDTO();
        dto.setId(equipo.getId());
        dto.setNombre(equipo.getNombre());
        dto.setPartidoId(equipo.getPartido().getId());
        dto.setCantidadParticipantes(equipo.getCantidadParticipantes());
        
        List<ParticipanteResponseDTO> participantesDTO = equipo.getParticipantes().stream()
                .map(this::convertirParticipanteADTO)
                .collect(Collectors.toList());
        dto.setParticipantes(participantesDTO);
        
        return dto;
    }

    private ParticipanteResponseDTO convertirParticipanteADTO(Participante participante) {
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

