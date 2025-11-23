package com.techlab.picadito.repository;

import com.techlab.picadito.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    List<Reserva> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    
    List<Reserva> findByUsuarioIdAndEstado(Long usuarioId, Reserva.EstadoReserva estado);
    
    List<Reserva> findByEstado(Reserva.EstadoReserva estado);
}

