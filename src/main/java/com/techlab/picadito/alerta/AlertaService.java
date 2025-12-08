package com.techlab.picadito.alerta;

import com.techlab.picadito.dto.AlertaDTO;
import com.techlab.picadito.dto.AlertaResponseDTO;
import com.techlab.picadito.dto.AlertasResponseDTO;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Alerta;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.TipoAlerta;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.partido.PartidoService;
import com.techlab.picadito.usuario.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertaService {

    private static final Logger logger = LoggerFactory.getLogger(AlertaService.class);
    private static final int UMBRAL_CUPOS_BAJOS = 5;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    @Lazy
    private PartidoService partidoService;

    public AlertasResponseDTO obtenerPorUsuario(@NonNull Long usuarioId) {
        logger.debug("Obteniendo alertas del usuario {}", usuarioId);
        List<AlertaResponseDTO> alertas = alertaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return new AlertasResponseDTO(alertas);
    }

    public AlertasResponseDTO obtenerNoLeidasPorUsuario(@NonNull Long usuarioId) {
        logger.debug("Obteniendo alertas no leídas del usuario {}", usuarioId);
        List<AlertaResponseDTO> alertas = alertaRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return new AlertasResponseDTO(alertas);
    }

    public AlertaResponseDTO crear(AlertaDTO alertaDTO) {
        logger.info("Creando nueva alerta de tipo: {}", alertaDTO.getTipo());
        
        Alerta alerta = new Alerta();
        alerta.setTipo(alertaDTO.getTipo());
        alerta.setMensaje(alertaDTO.getMensaje());
        alerta.setLeida(false);

        if (alertaDTO.getUsuarioId() != null) {
            Usuario usuario = usuarioService.obtenerUsuarioEntity(alertaDTO.getUsuarioId());
            alerta.setUsuario(usuario);
        }

        if (alertaDTO.getPartidoId() != null) {
            Long partidoId = Objects.requireNonNull(alertaDTO.getPartidoId(), "Partido ID no puede ser null");
            Partido partido = partidoService.obtenerPartidoEntity(partidoId);
            alerta.setPartido(partido);
        }

        alerta = alertaRepository.save(alerta);
        logger.info("Alerta creada exitosamente con id: {}", alerta.getId());
        return convertirADTO(alerta);
    }

    public void crearAlertaCuposBajos(Partido partido) {
        int cuposDisponibles = partido.getMaxJugadores() - partido.getCantidadParticipantes();
        if (cuposDisponibles <= UMBRAL_CUPOS_BAJOS && cuposDisponibles > 0) {
            AlertaDTO alertaDTO = new AlertaDTO();
            alertaDTO.setTipo(TipoAlerta.CUPOS_BAJOS);
            alertaDTO.setMensaje(String.format("El partido '%s' tiene solo %d cupos disponibles", 
                    partido.getTitulo(), cuposDisponibles));
            Long partidoId = Objects.requireNonNull(partido.getId(), "El partido debe tener un ID");
            alertaDTO.setPartidoId(partidoId);
            crear(alertaDTO);
        }
    }

    public void crearAlertaPartidoProximo(Partido partido, Long usuarioId) {
        AlertaDTO alertaDTO = new AlertaDTO();
        alertaDTO.setTipo(TipoAlerta.PARTIDO_PROXIMO);
        alertaDTO.setMensaje(String.format("El partido '%s' se jugará pronto. Fecha: %s", 
                partido.getTitulo(), partido.getFechaHora()));
        alertaDTO.setPartidoId(partido.getId());
        alertaDTO.setUsuarioId(usuarioId);
        crear(alertaDTO);
    }

    public void crearAlertaReservaConfirmada(Long usuarioId, String tituloPartido) {
        AlertaDTO alertaDTO = new AlertaDTO();
        alertaDTO.setTipo(TipoAlerta.RESERVA_CONFIRMADA);
        alertaDTO.setMensaje(String.format("Tu reserva para el partido '%s' ha sido confirmada", tituloPartido));
        alertaDTO.setUsuarioId(usuarioId);
        crear(alertaDTO);
    }

    public AlertaResponseDTO marcarComoLeida(@NonNull Long id) {
        logger.info("Marcando alerta {} como leída", id);
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + id));
        alerta.setLeida(true);
        alerta = alertaRepository.save(alerta);
        return convertirADTO(alerta);
    }

    public void marcarTodasComoLeidas(@NonNull Long usuarioId) {
        logger.info("Marcando todas las alertas del usuario {} como leídas", usuarioId);
        alertaRepository.marcarTodasComoLeidas(usuarioId);
    }

    public void eliminar(@NonNull Long id) {
        logger.info("Eliminando alerta con id: {}", id);
        if (!alertaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alerta no encontrada con id: " + id);
        }
        alertaRepository.deleteById(id);
        logger.info("Alerta eliminada exitosamente");
    }

    public void eliminarAlertasAntiguas(int diasAntiguedad) {
        logger.info("Eliminando alertas más antiguas de {} días", diasAntiguedad);
        java.time.LocalDateTime fechaLimite = java.time.LocalDateTime.now().minusDays(diasAntiguedad);
        List<Alerta> alertasAntiguas = alertaRepository.findAlertasAntiguas(fechaLimite);
        if (alertasAntiguas != null && !alertasAntiguas.isEmpty()) {
            alertaRepository.deleteAll(alertasAntiguas);
            logger.info("Se eliminaron {} alertas antiguas", alertasAntiguas.size());
        } else {
            logger.info("No se encontraron alertas antiguas para eliminar");
        }
    }

    private AlertaResponseDTO convertirADTO(Alerta alerta) {
        AlertaResponseDTO dto = new AlertaResponseDTO();
        dto.setId(alerta.getId());
        dto.setTipo(alerta.getTipo());
        dto.setMensaje(alerta.getMensaje());
        dto.setLeida(alerta.getLeida());
        dto.setFechaCreacion(alerta.getFechaCreacion());
        
        if (alerta.getUsuario() != null) {
            dto.setUsuarioId(alerta.getUsuario().getId());
        }
        
        if (alerta.getPartido() != null) {
            dto.setPartidoId(alerta.getPartido().getId());
            dto.setPartidoTitulo(alerta.getPartido().getTitulo());
        }
        
        return dto;
    }
}

