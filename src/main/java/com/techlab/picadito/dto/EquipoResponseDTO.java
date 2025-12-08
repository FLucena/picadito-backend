package com.techlab.picadito.dto;

import java.util.List;

public class EquipoResponseDTO {

    private Long id;
    private String nombre;
    private Long partidoId;
    private Integer cantidadParticipantes;
    private List<ParticipanteResponseDTO> participantes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getPartidoId() {
        return partidoId;
    }

    public void setPartidoId(Long partidoId) {
        this.partidoId = partidoId;
    }

    public Integer getCantidadParticipantes() {
        return cantidadParticipantes;
    }

    public void setCantidadParticipantes(Integer cantidadParticipantes) {
        this.cantidadParticipantes = cantidadParticipantes;
    }

    public List<ParticipanteResponseDTO> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<ParticipanteResponseDTO> participantes) {
        this.participantes = participantes;
    }
}

