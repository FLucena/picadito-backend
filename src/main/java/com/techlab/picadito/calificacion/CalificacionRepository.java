package com.techlab.picadito.calificacion;

import com.techlab.picadito.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    
    List<Calificacion> findByPartidoIdOrderByFechaCreacionDesc(Long partidoId);
    
    Optional<Calificacion> findByUsuarioIdAndPartidoId(Long usuarioId, Long partidoId);
    
    boolean existsByUsuarioIdAndPartidoId(Long usuarioId, Long partidoId);
    
    @Query("SELECT AVG(c.puntuacion) FROM Calificacion c WHERE c.partido.id = :partidoId")
    Double calcularPromedioPorPartido(@Param("partidoId") Long partidoId);
    
    @Query("SELECT AVG(c.puntuacion) FROM Calificacion c WHERE c.partido.creadorNombre = :creadorNombre")
    Double calcularPromedioPorCreador(@Param("creadorNombre") String creadorNombre);
    
    @Query("SELECT AVG(c.puntuacion) FROM Calificacion c WHERE c.partido.sede.id = :sedeId")
    Double calcularPromedioPorSede(@Param("sedeId") Long sedeId);
}

