package com.techlab.picadito.service;

import com.techlab.picadito.dto.PartidosSeleccionadosDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.LineaPartidoSeleccionado;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.PartidosSeleccionados;
import com.techlab.picadito.repository.PartidosSeleccionadosRepository;
import com.techlab.picadito.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PartidosSeleccionadosService {
    
    private final PartidosSeleccionadosRepository partidosSeleccionadosRepository;
    private final UsuarioService usuarioService;
    private final PartidoService partidoService;
    private final MapperUtil mapperUtil;
    
    public PartidosSeleccionadosDTO obtenerPartidosSeleccionadosPorUsuario(Long usuarioId) {
        PartidosSeleccionados partidosSeleccionados = partidosSeleccionadosRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearPartidosSeleccionados(usuarioId));
        return mapperUtil.toPartidosSeleccionadosDTO(partidosSeleccionados);
    }
    
    @Transactional
    public PartidosSeleccionados crearPartidosSeleccionados(Long usuarioId) {
        PartidosSeleccionados partidosSeleccionados = new PartidosSeleccionados();
        partidosSeleccionados.setUsuario(usuarioService.obtenerUsuarioEntity(usuarioId));
        return partidosSeleccionadosRepository.save(partidosSeleccionados);
    }
    
    @Transactional
    public PartidosSeleccionadosDTO agregarPartido(Long usuarioId, Long partidoId, Integer cantidad) {
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser null");
        Objects.requireNonNull(partidoId, "El ID del partido no puede ser null");
        Objects.requireNonNull(cantidad, "La cantidad no puede ser null");
        
        PartidosSeleccionados partidosSeleccionados = partidosSeleccionadosRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearPartidosSeleccionados(usuarioId));
        
        Partido partido = partidoService.obtenerPartidoEntity(partidoId);
        
        // Validar que el partido esté disponible
        if (partido.getEstado() != EstadoPartido.DISPONIBLE) {
            throw new BusinessException("El partido no está disponible. Estado actual: " + partido.getEstado());
        }
        
        // Validar que no esté completo
        if (partido.estaCompleto()) {
            throw new BusinessException("El partido ya está completo. Máximo de jugadores: " + partido.getMaxJugadores());
        }
        
        // Validar capacidad disponible
        int capacidadDisponible = partido.getMaxJugadores() - partido.getCantidadParticipantes();
        if (cantidad > capacidadDisponible) {
            throw new BusinessException("No hay suficiente capacidad disponible. Capacidad disponible: " + capacidadDisponible);
        }
        
        LineaPartidoSeleccionado lineaExistente = partidosSeleccionados.getItems().stream()
                .filter(item -> item.getPartido().getId().equals(partidoId))
                .findFirst()
                .orElse(null);
        
        if (lineaExistente != null) {
            int nuevaCantidad = lineaExistente.getCantidad() + cantidad;
            if (nuevaCantidad > capacidadDisponible) {
                throw new BusinessException("No hay suficiente capacidad disponible. Capacidad disponible: " + capacidadDisponible);
            }
            lineaExistente.setCantidad(nuevaCantidad);
        } else {
            LineaPartidoSeleccionado nuevaLinea = new LineaPartidoSeleccionado();
            nuevaLinea.setPartidosSeleccionados(partidosSeleccionados);
            nuevaLinea.setPartido(partido);
            nuevaLinea.setCantidad(cantidad);
            partidosSeleccionados.getItems().add(nuevaLinea);
        }
        
        partidosSeleccionados = partidosSeleccionadosRepository.save(partidosSeleccionados);
        return mapperUtil.toPartidosSeleccionadosDTO(partidosSeleccionados);
    }
    
    @Transactional
    public PartidosSeleccionadosDTO actualizarCantidad(Long usuarioId, Long lineaPartidoSeleccionadoId, Integer cantidad) {
        PartidosSeleccionados partidosSeleccionados = partidosSeleccionadosRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException("Partidos seleccionados no encontrados"));
        
        LineaPartidoSeleccionado linea = partidosSeleccionados.getItems().stream()
                .filter(item -> item.getId().equals(lineaPartidoSeleccionadoId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Partido no encontrado en la selección"));
        
        if (cantidad <= 0) {
            partidosSeleccionados.getItems().remove(linea);
        } else {
            Partido partido = linea.getPartido();
            int capacidadDisponible = partido.getMaxJugadores() - partido.getCantidadParticipantes();
            if (cantidad > capacidadDisponible) {
                throw new BusinessException("No hay suficiente capacidad disponible. Capacidad disponible: " + capacidadDisponible);
            }
            linea.setCantidad(cantidad);
        }
        
        partidosSeleccionados = partidosSeleccionadosRepository.save(partidosSeleccionados);
        return mapperUtil.toPartidosSeleccionadosDTO(partidosSeleccionados);
    }
    
    @Transactional
    public void eliminarItem(Long usuarioId, Long lineaPartidoSeleccionadoId) {
        PartidosSeleccionados partidosSeleccionados = partidosSeleccionadosRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException("Partidos seleccionados no encontrados"));
        
        partidosSeleccionados.getItems().removeIf(item -> item.getId().equals(lineaPartidoSeleccionadoId));
        partidosSeleccionadosRepository.save(partidosSeleccionados);
    }
    
    @Transactional
    public void vaciarPartidosSeleccionados(Long usuarioId) {
        PartidosSeleccionados partidosSeleccionados = partidosSeleccionadosRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException("Partidos seleccionados no encontrados"));
        
        partidosSeleccionados.getItems().clear();
        partidosSeleccionadosRepository.save(partidosSeleccionados);
    }
}

