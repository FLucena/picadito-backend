package com.techlab.picadito.dto;

import java.util.List;

/**
 * DTO wrapper para listas de alertas.
 * Facilita agregar paginación, metadatos y otros campos en el futuro.
 */
public class AlertasResponseDTO {
    
    private List<AlertaResponseDTO> alertas;
    private int total;
    private int noLeidas;
    
    public AlertasResponseDTO() {
    }
    
    public AlertasResponseDTO(List<AlertaResponseDTO> alertas) {
        this.alertas = alertas;
        this.total = alertas != null ? alertas.size() : 0;
        this.noLeidas = alertas != null ? (int) alertas.stream().filter(a -> !a.getLeida()).count() : 0;
    }
    
    public List<AlertaResponseDTO> getAlertas() {
        return alertas;
    }
    
    public void setAlertas(List<AlertaResponseDTO> alertas) {
        this.alertas = alertas;
        this.total = alertas != null ? alertas.size() : 0;
        this.noLeidas = alertas != null ? (int) alertas.stream().filter(a -> !a.getLeida()).count() : 0;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public int getNoLeidas() {
        return noLeidas;
    }
    
    public void setNoLeidas(int noLeidas) {
        this.noLeidas = noLeidas;
    }
}

