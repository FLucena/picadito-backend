package com.techlab.picadito.reserva;

import com.techlab.picadito.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    List<Reserva> findAllByOrderByFechaCreacionDesc();
    
    List<Reserva> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    
    List<Reserva> findByUsuarioIdAndEstado(Long usuarioId, Reserva.EstadoReserva estado);
    
    List<Reserva> findByEstado(Reserva.EstadoReserva estado);
    
    List<Reserva> findByEstadoInOrderByFechaCreacionDesc(List<Reserva.EstadoReserva> estados);
    
    List<Reserva> findByFechaCreacionBetweenOrderByFechaCreacionDesc(java.time.LocalDateTime fechaInicio, java.time.LocalDateTime fechaFin);
    
    List<Reserva> findByEstadoInAndFechaCreacionBetween(List<Reserva.EstadoReserva> estados, java.time.LocalDateTime fechaInicio, java.time.LocalDateTime fechaFin);
}

