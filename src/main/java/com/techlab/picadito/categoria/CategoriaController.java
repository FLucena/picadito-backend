package com.techlab.picadito.categoria;

import com.techlab.picadito.dto.CategoriaDTO;
import com.techlab.picadito.dto.CategoriaResponseDTO;
import com.techlab.picadito.dto.CategoriasResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<CategoriasResponseDTO> obtenerTodas() {
        CategoriasResponseDTO categorias = categoriaService.obtenerTodas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        CategoriaResponseDTO categoria = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(categoria);
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaDTO dto) {
        CategoriaResponseDTO categoria = categoriaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id,
            @Valid @RequestBody CategoriaDTO dto) {
        CategoriaResponseDTO categoria = categoriaService.actualizar(id, dto);
        return ResponseEntity.ok(categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

