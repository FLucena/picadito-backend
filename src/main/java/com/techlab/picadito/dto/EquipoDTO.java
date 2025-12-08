package com.techlab.picadito.dto;

import java.util.List;

public class EquipoDTO {

    private String nombre;
    private List<Long> participanteIds;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Long> getParticipanteIds() {
        return participanteIds;
    }

    public void setParticipanteIds(List<Long> participanteIds) {
        this.participanteIds = participanteIds;
    }
}

