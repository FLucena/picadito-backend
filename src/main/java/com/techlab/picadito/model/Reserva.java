package com.techlab.picadito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservas", indexes = {
    @Index(name = "idx_reservas_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_reservas_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaReserva> lineasReserva = new ArrayList<>();
    
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
    
    public enum EstadoReserva {
        PENDIENTE,      // Reservas pendientes de confirmación
        CONFIRMADO,     // Reservas confirmadas (participantes ya inscritos)
        EN_PROCESO,     // El partido está próximo a jugarse o en curso
        FINALIZADO,     // El partido ya se jugó
        CANCELADO       // Reserva cancelada
    }

    /**
     * Calcula el total de la reserva sumando precio × cantidad de cada línea de reserva
     * @return El total calculado, 0.0 si no hay líneas o precios
     */
    public Double calcularTotal() {
        if (lineasReserva == null || lineasReserva.isEmpty()) {
            return 0.0;
        }
        
        return lineasReserva.stream()
                .mapToDouble(linea -> {
                    if (linea.getPartido() == null || linea.getPartido().getPrecio() == null) {
                        return 0.0;
                    }
                    Double precio = linea.getPartido().getPrecio();
                    Integer cantidad = linea.getCantidad() != null ? linea.getCantidad() : 0;
                    return precio * cantidad;
                })
                .sum();
    }
}

