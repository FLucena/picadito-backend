package com.techlab.picadito.repository;

import com.techlab.picadito.model.Participante;
import com.techlab.picadito.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {
    List<Participante> findByPartido(Partido partido);
    List<Participante> findByPartidoId(Long partidoId);
    Optional<Participante> findByPartidoAndNombre(Partido partido, String nombre);
    boolean existsByPartidoAndNombre(Partido partido, String nombre);
}

