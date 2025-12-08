package com.techlab.picadito.partidosguardados;

import com.techlab.picadito.dto.PartidosGuardadosResponseDTO;
import com.techlab.picadito.dto.LineaPartidoGuardadoDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.PartidosGuardados;
import com.techlab.picadito.model.LineaPartidoGuardado;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.partido.PartidoRepository;
import com.techlab.picadito.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class PartidosGuardadosService {

    private static final Logger logger = LoggerFactory.getLogger(PartidosGuardadosService.class);

    @Autowired
    private PartidosGuardadosRepository partidosGuardadosRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public PartidosGuardadosResponseDTO obtenerPartidosGuardadosPorUsuario(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser null");
        logger.debug("Buscando partidos guardados para usuario: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        PartidosGuardados partidosGuardados = partidosGuardadosRepository.findByUsuario(usuario)
                .orElseGet(() -> {
                    logger.debug("No existe partidos guardados para usuario {}, creando uno nuevo", usuarioId);
                    PartidosGuardados nuevosPartidosGuardados = new PartidosGuardados();
                    nuevosPartidosGuardados.setUsuario(usuario);
                    return partidosGuardadosRepository.save(nuevosPartidosGuardados);
                });

        return convertirADTO(partidosGuardados);
    }

    public PartidosGuardadosResponseDTO agregarPartido(Long usuarioId, Long partidoId) {
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser null");
        Objects.requireNonNull(partidoId, "El ID del partido no puede ser null");
        logger.info("Agregando partido {} a los partidos guardados del usuario {}", partidoId, usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + partidoId));

        // Validar que el partido esté disponible
        if (partido.getEstado() != EstadoPartido.DISPONIBLE) {
            throw new BusinessException("No se puede agregar un partido que no está disponible. Estado actual: " + partido.getEstado());
        }

        // Validar que no esté completo
        if (partido.estaCompleto()) {
            throw new BusinessException("El partido ya está completo. Máximo de jugadores: " + partido.getMaxJugadores());
        }

        // Obtener o crear partidos guardados
        PartidosGuardados partidosGuardados = partidosGuardadosRepository.findByUsuario(usuario)
                .orElseGet(() -> {
                    PartidosGuardados nuevosPartidosGuardados = new PartidosGuardados();
                    nuevosPartidosGuardados.setUsuario(usuario);
                    return partidosGuardadosRepository.save(nuevosPartidosGuardados);
                });

        // Validar que el partido no esté ya en los partidos guardados
        boolean yaExiste = partidosGuardados.getPartidos().stream()
                .anyMatch(linea -> linea.getPartido().getId().equals(partidoId));

        if (yaExiste) {
            throw new BusinessException("El partido ya está en tu lista de partidos guardados");
        }

        // Agregar partido a los partidos guardados
        LineaPartidoGuardado lineaPartidoGuardado = new LineaPartidoGuardado();
        lineaPartidoGuardado.setPartidosGuardados(partidosGuardados);
        lineaPartidoGuardado.setPartido(partido);
        lineaPartidoGuardado.setCantidad(1);

        partidosGuardados.getPartidos().add(lineaPartidoGuardado);
        partidosGuardados = partidosGuardadosRepository.save(partidosGuardados);

        logger.info("Partido {} agregado exitosamente a los partidos guardados del usuario {}", partidoId, usuarioId);
        return convertirADTO(partidosGuardados);
    }

    public PartidosGuardadosResponseDTO eliminarPartido(Long usuarioId, Long lineaPartidoGuardadoId) {
        logger.info("Eliminando línea de partido guardado {} del usuario {}", lineaPartidoGuardadoId, usuarioId);

        PartidosGuardados partidosGuardados = partidosGuardadosRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Partidos guardados no encontrados para el usuario: " + usuarioId));

        LineaPartidoGuardado lineaAEliminar = partidosGuardados.getPartidos().stream()
                .filter(linea -> linea.getId().equals(lineaPartidoGuardadoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Línea de partido guardado no encontrada con id: " + lineaPartidoGuardadoId));

        partidosGuardados.getPartidos().remove(lineaAEliminar);
        partidosGuardados = partidosGuardadosRepository.save(partidosGuardados);

        logger.info("Línea de partido guardado {} eliminada exitosamente", lineaPartidoGuardadoId);
        return convertirADTO(partidosGuardados);
    }

    public void vaciarPartidosGuardados(Long usuarioId) {
        logger.info("Vaciando partidos guardados del usuario {}", usuarioId);

        PartidosGuardados partidosGuardados = partidosGuardadosRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Partidos guardados no encontrados para el usuario: " + usuarioId));

        partidosGuardados.getPartidos().clear();
        partidosGuardadosRepository.save(partidosGuardados);

        logger.info("Partidos guardados del usuario {} vaciados exitosamente", usuarioId);
    }

    private PartidosGuardadosResponseDTO convertirADTO(PartidosGuardados partidosGuardados) {
        PartidosGuardadosResponseDTO dto = new PartidosGuardadosResponseDTO();
        dto.setId(partidosGuardados.getId());
        dto.setUsuarioId(partidosGuardados.getUsuario().getId());
        dto.setUsuarioNombre(partidosGuardados.getUsuario().getNombre());
        dto.setFechaCreacion(partidosGuardados.getFechaCreacion());
        dto.setFechaActualizacion(partidosGuardados.getFechaActualizacion());

        dto.setPartidos(partidosGuardados.getPartidos().stream()
                .map(this::convertirLineaADTO)
                .collect(Collectors.toList()));

        dto.setTotalPartidos(partidosGuardados.getPartidos().size());
        return dto;
    }

    private LineaPartidoGuardadoDTO convertirLineaADTO(LineaPartidoGuardado linea) {
        LineaPartidoGuardadoDTO dto = new LineaPartidoGuardadoDTO();
        dto.setId(linea.getId());
        dto.setPartidoId(linea.getPartido().getId());
        dto.setPartidoTitulo(linea.getPartido().getTitulo());
        // Priorizar información de sede si existe, sino usar ubicación legacy
        if (linea.getPartido().getSede() != null && linea.getPartido().getSede().getNombre() != null) {
            dto.setPartidoUbicacion(linea.getPartido().getSede().getNombre());
        } else {
            dto.setPartidoUbicacion(linea.getPartido().getUbicacion());
        }
        dto.setPartidoFechaHora(linea.getPartido().getFechaHora().toString());
        dto.setCantidadParticipantes(linea.getPartido().getCantidadParticipantes());
        dto.setMaxJugadores(linea.getPartido().getMaxJugadores());
        dto.setCantidad(linea.getCantidad());
        return dto;
    }
}

