package com.techlab.picadito.dto;

import com.techlab.picadito.model.EstadoInscripcion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionConfirmadaResponseDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private EstadoInscripcion estado;
    private Integer totalPartidos;
    private List<LineaInscripcionDTO> lineasInscripcion = new ArrayList<>();
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

