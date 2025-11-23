package com.techlab.picadito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaPartidoGuardadoDTO {
    private Long id;
    private Long partidoId;
    private String partidoTitulo;
    private String partidoUbicacion;
    private String partidoFechaHora;
    private Integer cantidadParticipantes;
    private Integer maxJugadores;
    private Integer cantidad = 1;
}

