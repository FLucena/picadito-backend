package com.techlab.picadito.dto;

import com.techlab.picadito.model.Nivel;
import com.techlab.picadito.model.Posicion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParticipanteDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apodo no puede exceder 100 caracteres")
    private String apodo;

    private Posicion posicion;

    private Nivel nivel;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Normalizar strings vacíos a null para apodo
    public String getApodo() {
        return (apodo != null && apodo.trim().isEmpty()) ? null : apodo;
    }

    public void setApodo(String apodo) {
        this.apodo = (apodo != null && apodo.trim().isEmpty()) ? null : apodo;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public void setPosicion(Posicion posicion) {
        this.posicion = posicion;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }
}

