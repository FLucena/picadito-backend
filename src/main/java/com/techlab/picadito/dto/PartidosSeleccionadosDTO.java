package com.techlab.picadito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidosSeleccionadosDTO {
    
    private Long id;
    
    private Long usuarioId;
    
    private List<LineaPartidoSeleccionadoDTO> items = new ArrayList<>();
    
    private Integer totalPartidos;
    
    private LocalDateTime fechaCreacion;
    
    private LocalDateTime fechaActualizacion;
}

