package com.techlab.picadito.dto;

import com.techlab.picadito.model.EstadoPartido;
import java.time.LocalDateTime;
import java.util.List;

public class PartidoResponseDTO {

    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaHora;
    private String ubicacion;
    private Long sedeId;
    private SedeResponseDTO sede;
    private Integer maxJugadores;
    private EstadoPartido estado;
    private String creadorNombre;
    private LocalDateTime fechaCreacion;
    private Integer cantidadParticipantes;
    private List<ParticipanteResponseDTO> participantes;
    private Double precio;
    private String imagenUrl;
    private List<Long> categoriaIds;
    private List<CategoriaResponseDTO> categorias;
    private Double promedioCalificacion;
    private List<EquipoResponseDTO> equipos;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public void setSedeId(Long sedeId) {
        this.sedeId = sedeId;
    }

    public SedeResponseDTO getSede() {
        return sede;
    }

    public void setSede(SedeResponseDTO sede) {
        this.sede = sede;
    }

    public Integer getMaxJugadores() {
        return maxJugadores;
    }

    public void setMaxJugadores(Integer maxJugadores) {
        this.maxJugadores = maxJugadores;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartido estado) {
        this.estado = estado;
    }

    public String getCreadorNombre() {
        return creadorNombre;
    }

    public void setCreadorNombre(String creadorNombre) {
        this.creadorNombre = creadorNombre;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public List<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public void setCategoriaIds(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }

    public List<CategoriaResponseDTO> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaResponseDTO> categorias) {
        this.categorias = categorias;
    }

    public Double getPromedioCalificacion() {
        return promedioCalificacion;
    }

    public void setPromedioCalificacion(Double promedioCalificacion) {
        this.promedioCalificacion = promedioCalificacion;
    }

    public List<EquipoResponseDTO> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<EquipoResponseDTO> equipos) {
        this.equipos = equipos;
    }
}

