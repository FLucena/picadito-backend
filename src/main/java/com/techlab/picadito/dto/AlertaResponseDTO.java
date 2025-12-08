package com.techlab.picadito.dto;

import com.techlab.picadito.model.TipoAlerta;
import java.time.LocalDateTime;

public class AlertaResponseDTO {

    private Long id;
    private TipoAlerta tipo;
    private String mensaje;
    private Boolean leida;
    private Long usuarioId;
    private Long partidoId;
    private String partidoTitulo;
    private LocalDateTime fechaCreacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public void setTipo(TipoAlerta tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getPartidoId() {
        return partidoId;
    }

    public void setPartidoId(Long partidoId) {
        this.partidoId = partidoId;
    }

    public String getPartidoTitulo() {
        return partidoTitulo;
    }

    public void setPartidoTitulo(String partidoTitulo) {
        this.partidoTitulo = partidoTitulo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

