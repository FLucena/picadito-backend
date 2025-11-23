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
public class PartidosGuardadosDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private List<LineaPartidoGuardadoDTO> partidos = new ArrayList<>();
    private Integer totalPartidos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

