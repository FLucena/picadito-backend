package com.techlab.picadito.dto;

import java.util.List;

/**
 * DTO wrapper para listas de sedes.
 * Facilita agregar paginación, metadatos y otros campos en el futuro.
 */
public class SedesResponseDTO {
    
    private List<SedeResponseDTO> sedes;
    private int total;
    
    public SedesResponseDTO() {
    }
    
    public SedesResponseDTO(List<SedeResponseDTO> sedes) {
        this.sedes = sedes;
        this.total = sedes != null ? sedes.size() : 0;
    }
    
    public List<SedeResponseDTO> getSedes() {
        return sedes;
    }
    
    public void setSedes(List<SedeResponseDTO> sedes) {
        this.sedes = sedes;
        this.total = sedes != null ? sedes.size() : 0;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
}

