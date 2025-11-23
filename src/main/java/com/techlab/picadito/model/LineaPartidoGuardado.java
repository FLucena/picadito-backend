package com.techlab.picadito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lineas_partido_guardado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaPartidoGuardado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partidos_guardados_id", nullable = false)
    private PartidosGuardados partidosGuardados;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;
    
    @Column(nullable = false)
    private Integer cantidad = 1; // Cantidad de inscripciones para este partido (normalmente 1)
}

