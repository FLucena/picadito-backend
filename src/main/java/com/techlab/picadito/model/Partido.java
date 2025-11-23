package com.techlab.picadito.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partidos")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es requerido")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "La fecha y hora son requeridas")
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Size(max = 300, message = "La ubicación no puede exceder 300 caracteres")
    @Column(nullable = true, length = 300)
    private String ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id", nullable = true)
    private Sede sede;

    @NotNull(message = "El número máximo de jugadores es requerido")
    @Min(value = 1, message = "El número máximo de jugadores debe ser al menos 1")
    @Max(value = 50, message = "El número máximo de jugadores no puede exceder 50")
    @Column(nullable = false)
    private Integer maxJugadores = 22;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPartido estado = EstadoPartido.DISPONIBLE;

    @NotBlank(message = "El nombre del creador es requerido")
    @Size(max = 100, message = "El nombre del creador no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String creadorNombre;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = true)
    private Double precio;

    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    @Column(nullable = true, length = 500)
    private String imagenUrl;

    @OneToMany(mappedBy = "partido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participante> participantes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

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

    public List<Participante> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<Participante> participantes) {
        this.participantes = participantes;
    }

    public Integer getCantidadParticipantes() {
        return participantes != null ? participantes.size() : 0;
    }

    public boolean estaCompleto() {
        return getCantidadParticipantes() >= maxJugadores;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public String getUbicacionDisplay() {
        return sede != null && sede.getNombre() != null ? sede.getNombre() : ubicacion;
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
}

