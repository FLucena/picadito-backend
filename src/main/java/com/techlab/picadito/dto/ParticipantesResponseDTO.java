package com.techlab.picadito.dto;

import java.util.List;

/**
 * DTO wrapper para listas de participantes.
 * Facilita agregar paginación, metadatos y otros campos en el futuro.
 */
public class ParticipantesResponseDTO {
    
    private List<ParticipanteResponseDTO> participantes;
    private int total;
    
    public ParticipantesResponseDTO() {
    }
    
    public ParticipantesResponseDTO(List<ParticipanteResponseDTO> participantes) {
        this.participantes = participantes;
        this.total = participantes != null ? participantes.size() : 0;
    }
    
    public List<ParticipanteResponseDTO> getParticipantes() {
        return participantes;
    }
    
    public void setParticipantes(List<ParticipanteResponseDTO> participantes) {
        this.participantes = participantes;
        this.total = participantes != null ? participantes.size() : 0;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
}

