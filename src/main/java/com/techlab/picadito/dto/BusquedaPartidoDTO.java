package com.techlab.picadito.dto;

import com.techlab.picadito.model.EstadoPartido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaPartidoDTO {
    private String titulo;
    private String ubicacion;
    private String creadorNombre;
    private EstadoPartido estado;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private Integer minJugadores;
    private Integer maxJugadores;
    private Integer cuposDisponiblesMin;
    private Boolean soloDisponibles;
}

