package com.techlab.picadito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lineas_partido_seleccionado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaPartidoSeleccionado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partidos_seleccionados_id", nullable = false)
    private PartidosSeleccionados partidosSeleccionados;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;
    
    @Column(nullable = false)
    private Integer cantidad = 1;
}

