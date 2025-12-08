package com.techlab.picadito.partidosseleccionados;

import com.techlab.picadito.model.PartidosSeleccionados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartidosSeleccionadosRepository extends JpaRepository<PartidosSeleccionados, Long> {
    
    Optional<PartidosSeleccionados> findByUsuarioId(Long usuarioId);
    
    boolean existsByUsuarioId(Long usuarioId);
}

