package com.techlab.picadito.equipo;

import com.techlab.picadito.dto.EquipoResponseDTO;
import com.techlab.picadito.dto.EquiposResponseDTO;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/equipos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @PostMapping("/partido/{partidoId}/generar")
    public ResponseEntity<EquiposResponseDTO> generarEquiposAutomaticos(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long partidoId) {
        EquiposResponseDTO equipos = equipoService.generarEquiposAutomaticos(partidoId);
        return ResponseEntity.ok(equipos);
    }

    @GetMapping("/partido/{partidoId}")
    public ResponseEntity<EquiposResponseDTO> obtenerEquiposPorPartido(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long partidoId) {
        EquiposResponseDTO equipos = equipoService.obtenerEquiposPorPartido(partidoId);
        return ResponseEntity.ok(equipos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipoResponseDTO> obtenerPorId(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        EquipoResponseDTO equipo = equipoService.obtenerPorId(id);
        return ResponseEntity.ok(equipo);
    }

    @DeleteMapping("/partido/{partidoId}")
    public ResponseEntity<Void> eliminarEquipos(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long partidoId) {
        equipoService.eliminarEquipos(partidoId);
        return ResponseEntity.noContent().build();
    }
}

