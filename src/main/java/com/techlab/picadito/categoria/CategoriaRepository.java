package com.techlab.picadito.categoria;

import com.techlab.picadito.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    List<Categoria> findAllByOrderByNombreAsc();
    
    Optional<Categoria> findByNombre(String nombre);
    
    boolean existsByNombre(String nombre);
}

