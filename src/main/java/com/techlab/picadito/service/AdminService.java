package com.techlab.picadito.service;

import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.repository.PartidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    
    private final PartidoRepository partidoRepository;
    private final PartidoService partidoService;
    
    /**
     * Obtiene partidos con capacidad disponible baja (equivalente a stock bajo)
     * 
     * @param capacidadMinima Capacidad mínima disponible para considerar un partido como crítico (default: 5)
     * @return Lista de partidos con capacidad baja, ordenados por capacidad disponible ascendente
     */
    public List<PartidoResponseDTO> obtenerPartidosConCapacidadBaja(Integer capacidadMinima) {
        final int capacidadMinimaFinal = (capacidadMinima == null) ? 5 : capacidadMinima;
        
        // Obtener todos los partidos disponibles
        List<Partido> partidosDisponibles = partidoRepository.findByEstado(EstadoPartido.DISPONIBLE);
        
        // Filtrar partidos con capacidad disponible <= capacidadMinima
        // y ordenar por capacidad disponible ascendente (más críticos primero)
        return partidosDisponibles.stream()
                .filter(partido -> {
                    int capacidadDisponible = partido.getMaxJugadores() - partido.getCantidadParticipantes();
                    return capacidadDisponible <= capacidadMinimaFinal;
                })
                .sorted(Comparator.comparingInt(partido -> 
                    partido.getMaxJugadores() - partido.getCantidadParticipantes()))
                .map(partido -> {
                    Long partidoId = Objects.requireNonNull(partido.getId(), "El ID del partido no puede ser null");
                    return partidoService.obtenerPartidoPorId(partidoId);
                })
                .collect(Collectors.toList());
    }
}

