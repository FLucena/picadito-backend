package com.techlab.picadito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lineas_inscripcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaInscripcion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private InscripcionConfirmada inscripcion;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInscripcion estado = EstadoInscripcion.PENDIENTE;
}

