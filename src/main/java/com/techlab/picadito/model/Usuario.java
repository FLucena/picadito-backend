package com.techlab.picadito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_usuarios_email", columnList = "email", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol = RolUsuario.CLIENTE;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;
    
    @Column(name = "cuenta_bloqueada")
    private Boolean cuentaBloqueada = false;
    
    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;
    
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
    
    public enum RolUsuario {
        ADMIN, CLIENTE
    }
}

