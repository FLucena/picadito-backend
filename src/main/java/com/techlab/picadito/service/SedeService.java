package com.techlab.picadito.service;

import com.techlab.picadito.dto.SedeDTO;
import com.techlab.picadito.dto.SedeResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Sede;
import com.techlab.picadito.repository.PartidoRepository;
import com.techlab.picadito.repository.SedeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class SedeService {

    private static final Logger logger = LoggerFactory.getLogger(SedeService.class);

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    public List<SedeResponseDTO> obtenerTodas() {
        logger.debug("Obteniendo todas las sedes");
        return sedeRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public SedeResponseDTO obtenerPorId(Long id) {
        Objects.requireNonNull(id, "El ID de la sede no puede ser null");
        logger.debug("Buscando sede con id: {}", id);
        
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + id));
        
        return convertirADTO(sede);
    }

    public SedeResponseDTO crear(SedeDTO dto) {
        logger.info("Creando nueva sede");
        
        // Validar que no exista otra sede con el mismo nombre (si se proporciona)
        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            sedeRepository.findByNombreIgnoreCase(dto.getNombre().trim())
                    .ifPresent(sede -> {
                        throw new BusinessException("Ya existe una sede con el nombre: " + dto.getNombre());
                    });
        }

        Sede sede = new Sede();
        sede.setNombre(dto.getNombre() != null ? dto.getNombre().trim() : null);
        sede.setDireccion(dto.getDireccion() != null ? dto.getDireccion().trim() : null);
        sede.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        sede.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        sede.setCoordenadas(dto.getCoordenadas() != null ? dto.getCoordenadas().trim() : null);

        sede = sedeRepository.save(sede);
        logger.info("Sede creada exitosamente con id: {}", sede.getId());
        return convertirADTO(sede);
    }

    public SedeResponseDTO actualizar(Long id, SedeDTO dto) {
        Objects.requireNonNull(id, "El ID de la sede no puede ser null");
        logger.info("Actualizando sede con id: {}", id);
        
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + id));
        Objects.requireNonNull(sede, "La sede no puede ser null");

        // Validar que no exista otra sede con el mismo nombre (si se proporciona y es diferente)
        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            sedeRepository.findByNombreIgnoreCase(dto.getNombre().trim())
                    .ifPresent(sedeExistente -> {
                        if (!sedeExistente.getId().equals(id)) {
                            throw new BusinessException("Ya existe otra sede con el nombre: " + dto.getNombre());
                        }
                    });
        }

        if (dto.getNombre() != null) {
            sede.setNombre(dto.getNombre().trim().isEmpty() ? null : dto.getNombre().trim());
        }
        if (dto.getDireccion() != null) {
            sede.setDireccion(dto.getDireccion().trim().isEmpty() ? null : dto.getDireccion().trim());
        }
        if (dto.getDescripcion() != null) {
            sede.setDescripcion(dto.getDescripcion().trim().isEmpty() ? null : dto.getDescripcion().trim());
        }
        if (dto.getTelefono() != null) {
            sede.setTelefono(dto.getTelefono().trim().isEmpty() ? null : dto.getTelefono().trim());
        }
        if (dto.getCoordenadas() != null) {
            sede.setCoordenadas(dto.getCoordenadas().trim().isEmpty() ? null : dto.getCoordenadas().trim());
        }

        Sede sedeGuardada = sedeRepository.save(Objects.requireNonNull(sede, "La sede no puede ser null"));
        logger.info("Sede actualizada exitosamente");
        return convertirADTO(sedeGuardada);
    }

    public void eliminar(Long id) {
        Objects.requireNonNull(id, "El ID de la sede no puede ser null");
        logger.info("Eliminando sede con id: {}", id);
        
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + id));

        // Validar que la sede no esté en uso
        List<Partido> partidosConSede = partidoRepository.findAll().stream()
                .filter(p -> p.getSede() != null && p.getSede().getId().equals(id))
                .collect(Collectors.toList());

        if (!partidosConSede.isEmpty()) {
            throw new BusinessException("No se puede eliminar la sede porque está siendo utilizada por " + 
                    partidosConSede.size() + " partido(s)");
        }

        Objects.requireNonNull(sede, "La sede no puede ser null");
        sedeRepository.delete(sede);
        logger.info("Sede eliminada exitosamente");
    }

    public Map<String, Object> migrarUbicacionesASedes() {
        logger.info("Iniciando migración de ubicaciones a sedes");
        
        // Obtener todos los partidos que no tienen sede asignada y tienen ubicación
        List<Partido> partidosSinSede = partidoRepository.findAll().stream()
                .filter(p -> p.getSede() == null && p.getUbicacion() != null && !p.getUbicacion().trim().isEmpty())
                .collect(Collectors.toList());

        if (partidosSinSede.isEmpty()) {
            logger.info("No hay partidos para migrar");
            return Map.of(
                    "sedesCreadas", 0,
                    "partidosActualizados", 0,
                    "mensaje", "No hay partidos para migrar"
            );
        }

        // Agrupar por ubicación única
        Map<String, List<Partido>> partidosPorUbicacion = partidosSinSede.stream()
                .collect(Collectors.groupingBy(p -> p.getUbicacion().trim()));

        int sedesCreadas = 0;
        int partidosActualizados = 0;

        for (Map.Entry<String, List<Partido>> entry : partidosPorUbicacion.entrySet()) {
            String ubicacion = entry.getKey();
            List<Partido> partidos = entry.getValue();

            // Verificar si ya existe una sede con esta dirección
            Sede sedeExistente = sedeRepository.findByDireccion(ubicacion).stream()
                    .findFirst()
                    .orElse(null);

            Sede sede;
            if (sedeExistente != null) {
                sede = sedeExistente;
                logger.debug("Usando sede existente con id: {} para ubicación: {}", sede.getId(), ubicacion);
            } else {
                // Crear nueva sede usando la ubicación como nombre y dirección
                sede = new Sede();
                sede.setNombre(ubicacion);
                sede.setDireccion(ubicacion);
                sede = sedeRepository.save(sede);
                sedesCreadas++;
                logger.debug("Sede creada con id: {} para ubicación: {}", sede.getId(), ubicacion);
            }

            // Asignar la sede a todos los partidos con esta ubicación
            for (Partido partido : partidos) {
                partido.setSede(sede);
                partidosActualizados++;
            }
        }

        // Guardar todos los partidos actualizados
        partidoRepository.saveAll(partidosSinSede);

        logger.info("Migración completada: {} sedes creadas, {} partidos actualizados", 
                sedesCreadas, partidosActualizados);

        return Map.of(
                "sedesCreadas", sedesCreadas,
                "partidosActualizados", partidosActualizados,
                "mensaje", String.format("Migración completada: %d sedes creadas, %d partidos actualizados", 
                        sedesCreadas, partidosActualizados)
        );
    }

    private SedeResponseDTO convertirADTO(Sede sede) {
        SedeResponseDTO dto = new SedeResponseDTO();
        dto.setId(sede.getId());
        dto.setNombre(sede.getNombre());
        dto.setDireccion(sede.getDireccion());
        dto.setDescripcion(sede.getDescripcion());
        dto.setTelefono(sede.getTelefono());
        dto.setCoordenadas(sede.getCoordenadas());
        dto.setFechaCreacion(sede.getFechaCreacion());
        dto.setFechaActualizacion(sede.getFechaActualizacion());
        return dto;
    }
}

