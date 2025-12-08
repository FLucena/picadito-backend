package com.techlab.picadito.partido;

import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long>, JpaSpecificationExecutor<Partido> {
    List<Partido> findByEstado(EstadoPartido estado);
    
    @EntityGraph(attributePaths = {"participantes", "sede", "categorias"})
    List<Partido> findByEstadoOrderByFechaHoraAsc(EstadoPartido estado);
    
    @EntityGraph(attributePaths = {"participantes", "sede", "categorias"})
    @Override
    @NonNull
    Optional<Partido> findById(@NonNull Long id);
    
    @EntityGraph(attributePaths = {"participantes", "sede", "categorias"})
    List<Partido> findAllByOrderByFechaHoraAsc();
    
    List<Partido> findBySedeId(Long sedeId);
    
    List<Partido> findBySedeIsNull();
    
    List<Partido> findBySedeIsNullAndUbicacionIsNotNull();
    
    List<Partido> findByFechaCreacionBetweenOrderByFechaCreacionAsc(java.time.LocalDateTime fechaInicio, java.time.LocalDateTime fechaFin);
    
    List<Partido> findByCantidadParticipantesGreaterThan(Integer cantidad);
}

