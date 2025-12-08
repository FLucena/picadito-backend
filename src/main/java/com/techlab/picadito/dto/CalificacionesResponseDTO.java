package com.techlab.picadito.dto;

import java.util.List;

/**
 * DTO wrapper para listas de calificaciones.
 * Facilita agregar paginación, metadatos y otros campos en el futuro.
 */
public class CalificacionesResponseDTO {
    
    private List<CalificacionResponseDTO> calificaciones;
    private int total;
    private Double promedio;
    
    public CalificacionesResponseDTO() {
    }
    
    public CalificacionesResponseDTO(List<CalificacionResponseDTO> calificaciones) {
        this.calificaciones = calificaciones;
        this.total = calificaciones != null ? calificaciones.size() : 0;
    }
    
    public List<CalificacionResponseDTO> getCalificaciones() {
        return calificaciones;
    }
    
    public void setCalificaciones(List<CalificacionResponseDTO> calificaciones) {
        this.calificaciones = calificaciones;
        this.total = calificaciones != null ? calificaciones.size() : 0;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public Double getPromedio() {
        return promedio;
    }
    
    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }
}

