package com.techlab.picadito.dto;

import java.util.List;

/**
 * DTO wrapper para listas de equipos.
 * Facilita agregar paginación, metadatos y otros campos en el futuro.
 */
public class EquiposResponseDTO {
    
    private List<EquipoResponseDTO> equipos;
    private int total;
    
    public EquiposResponseDTO() {
    }
    
    public EquiposResponseDTO(List<EquipoResponseDTO> equipos) {
        this.equipos = equipos;
        this.total = equipos != null ? equipos.size() : 0;
    }
    
    public List<EquipoResponseDTO> getEquipos() {
        return equipos;
    }
    
    public void setEquipos(List<EquipoResponseDTO> equipos) {
        this.equipos = equipos;
        this.total = equipos != null ? equipos.size() : 0;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
}

