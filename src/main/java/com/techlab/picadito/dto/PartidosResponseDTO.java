package com.techlab.picadito.dto;

import java.util.List;

/**
 * DTO wrapper para listas de partidos.
 * Facilita agregar paginación, metadatos y otros campos en el futuro.
 */
public class PartidosResponseDTO {
    
    private List<PartidoResponseDTO> partidos;
    private int total;
    
    public PartidosResponseDTO() {
    }
    
    public PartidosResponseDTO(List<PartidoResponseDTO> partidos) {
        this.partidos = partidos;
        this.total = partidos != null ? partidos.size() : 0;
    }
    
    public List<PartidoResponseDTO> getPartidos() {
        return partidos;
    }
    
    public void setPartidos(List<PartidoResponseDTO> partidos) {
        this.partidos = partidos;
        this.total = partidos != null ? partidos.size() : 0;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
}

