package com.techlab.picadito.util;

import com.techlab.picadito.dto.*;
import com.techlab.picadito.model.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MapperUtil {
    
    public LineaReservaDTO toLineaReservaDTO(LineaReserva linea) {
        LineaReservaDTO dto = new LineaReservaDTO();
        dto.setId(linea.getId());
        dto.setPartidoId(linea.getPartido().getId());
        dto.setPartidoTitulo(linea.getPartido().getTitulo());
        dto.setCantidad(linea.getCantidad());
        
        // Calcular subtotal: precio × cantidad
        Double precio = linea.getPartido().getPrecio();
        Integer cantidad = linea.getCantidad() != null ? linea.getCantidad() : 0;
        if (precio != null && cantidad > 0) {
            dto.setSubtotal(precio * cantidad);
        } else {
            dto.setSubtotal(0.0);
        }
        
        return dto;
    }
    
    public ReservaDTO toReservaDTO(Reserva reserva) {
        ReservaDTO dto = new ReservaDTO();
        dto.setId(reserva.getId());
        dto.setUsuarioId(reserva.getUsuario().getId());
        dto.setUsuarioNombre(reserva.getUsuario().getNombre());
        dto.setEstado(reserva.getEstado());
        dto.setLineasReserva(reserva.getLineasReserva().stream()
                .map(this::toLineaReservaDTO)
                .collect(Collectors.toList()));
        
        // Calcular total usando el método de la entidad
        dto.setTotal(reserva.calcularTotal());
        
        dto.setFechaCreacion(reserva.getFechaCreacion());
        dto.setFechaActualizacion(reserva.getFechaActualizacion());
        return dto;
    }
    
    public LineaPartidoSeleccionadoDTO toLineaPartidoSeleccionadoDTO(LineaPartidoSeleccionado linea) {
        LineaPartidoSeleccionadoDTO dto = new LineaPartidoSeleccionadoDTO();
        dto.setId(linea.getId());
        dto.setPartidoId(linea.getPartido().getId());
        dto.setPartidoTitulo(linea.getPartido().getTitulo());
        dto.setPartidoUbicacion(linea.getPartido().getUbicacion());
        dto.setCantidad(linea.getCantidad());
        return dto;
    }
    
    public PartidosSeleccionadosDTO toPartidosSeleccionadosDTO(PartidosSeleccionados partidosSeleccionados) {
        PartidosSeleccionadosDTO dto = new PartidosSeleccionadosDTO();
        dto.setId(partidosSeleccionados.getId());
        dto.setUsuarioId(partidosSeleccionados.getUsuario().getId());
        dto.setItems(partidosSeleccionados.getItems().stream()
                .map(this::toLineaPartidoSeleccionadoDTO)
                .collect(Collectors.toList()));
        dto.setTotalPartidos(partidosSeleccionados.getItems().size());
        dto.setFechaCreacion(partidosSeleccionados.getFechaCreacion());
        dto.setFechaActualizacion(partidosSeleccionados.getFechaActualizacion());
        return dto;
    }
}

