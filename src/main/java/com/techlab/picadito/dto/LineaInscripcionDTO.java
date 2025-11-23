package com.techlab.picadito.dto;

import com.techlab.picadito.model.EstadoInscripcion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaInscripcionDTO {
    private Long id;
    private Long partidoId;
    private String partidoTitulo;
    private String partidoUbicacion;
    private String partidoFechaHora;
    private Long participanteId;
    private String participanteNombre;
    private EstadoInscripcion estado;
}

