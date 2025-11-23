package com.techlab.picadito.repository;

import com.techlab.picadito.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {
    List<Sede> findByNombreContainingIgnoreCase(String nombre);
    List<Sede> findByDireccion(String direccion);
    Optional<Sede> findByNombreIgnoreCase(String nombre);
}

