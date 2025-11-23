package com.techlab.picadito.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaReservaDTO {
    
    private Long id;
    
    @NotNull(message = "El partido es obligatorio")
    private Long partidoId;
    
    private String partidoTitulo;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad = 1;
    
    private Double subtotal;
}

