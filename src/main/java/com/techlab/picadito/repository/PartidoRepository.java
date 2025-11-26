package com.techlab.picadito.repository;

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
    
    // Usar fetch join para evitar N+1 queries
    @EntityGraph(attributePaths = {"participantes", "sede", "categorias"})
    List<Partido> findByEstadoOrderByFechaHoraAsc(EstadoPartido estado);
    
    // Para obtener partido con participantes cargados
    @EntityGraph(attributePaths = {"participantes", "sede", "categorias"})
    @Override
    @NonNull
    Optional<Partido> findById(@NonNull Long id);
}

