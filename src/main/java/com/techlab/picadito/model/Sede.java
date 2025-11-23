package com.techlab.picadito.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sedes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sede {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(nullable = true, length = 200)
    private String nombre;
    
    @Size(max = 300, message = "La dirección no puede exceder 300 caracteres")
    @Column(nullable = true, length = 300)
    private String direccion;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(columnDefinition = "TEXT", nullable = true)
    private String descripcion;
    
    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    @Column(nullable = true, length = 50)
    private String telefono;
    
    @Size(max = 100, message = "Las coordenadas no pueden exceder 100 caracteres")
    @Column(nullable = true, length = 100)
    private String coordenadas;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}

