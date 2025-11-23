package com.techlab.picadito.repository;

import com.techlab.picadito.model.PartidosGuardados;
import com.techlab.picadito.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartidosGuardadosRepository extends JpaRepository<PartidosGuardados, Long> {
    Optional<PartidosGuardados> findByUsuario(Usuario usuario);
    Optional<PartidosGuardados> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
}

