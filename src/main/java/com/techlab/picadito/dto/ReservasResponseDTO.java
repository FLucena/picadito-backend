package com.techlab.picadito.dto;

import java.util.List;

/**
 * DTO wrapper para listas de reservas.
 * Facilita agregar paginación, metadatos y otros campos en el futuro.
 */
public class ReservasResponseDTO {
    
    private List<ReservaDTO> reservas;
    private int total;
    
    public ReservasResponseDTO() {
    }
    
    public ReservasResponseDTO(List<ReservaDTO> reservas) {
        this.reservas = reservas;
        this.total = reservas != null ? reservas.size() : 0;
    }
    
    public List<ReservaDTO> getReservas() {
        return reservas;
    }
    
    public void setReservas(List<ReservaDTO> reservas) {
        this.reservas = reservas;
        this.total = reservas != null ? reservas.size() : 0;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
}

