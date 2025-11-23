package com.techlab.picadito.repository;

import com.techlab.picadito.model.InscripcionConfirmada;
import com.techlab.picadito.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<InscripcionConfirmada, Long> {
    List<InscripcionConfirmada> findByUsuario(Usuario usuario);
    List<InscripcionConfirmada> findByUsuarioId(Long usuarioId);
    List<InscripcionConfirmada> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}

