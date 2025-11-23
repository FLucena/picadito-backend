package com.techlab.picadito.dto;

import com.techlab.picadito.model.Reserva;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {
    
    private Long id;
    
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;
    
    private String usuarioNombre;
    
    private Reserva.EstadoReserva estado;
    
    @NotEmpty(message = "La reserva debe tener al menos un partido")
    @Valid
    private List<LineaReservaDTO> lineasReserva = new ArrayList<>();
    
    private Double total;
    
    private LocalDateTime fechaCreacion;
    
    private LocalDateTime fechaActualizacion;
}

