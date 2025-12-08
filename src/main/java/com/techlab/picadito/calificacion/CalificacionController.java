package com.techlab.picadito.calificacion;

import com.techlab.picadito.dto.CalificacionDTO;
import com.techlab.picadito.dto.CalificacionResponseDTO;
import com.techlab.picadito.dto.CalificacionesResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/calificaciones")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class CalificacionController {

    @Autowired
    private CalificacionService calificacionService;

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<CalificacionResponseDTO> crear(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long usuarioId,
            @Valid @RequestBody CalificacionDTO dto) {
        CalificacionResponseDTO calificacion = calificacionService.crear(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(calificacion);
    }

    @GetMapping("/partido/{partidoId}")
    public ResponseEntity<CalificacionesResponseDTO> obtenerPorPartido(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long partidoId) {
        CalificacionesResponseDTO calificaciones = calificacionService.obtenerPorPartido(partidoId);
        return ResponseEntity.ok(calificaciones);
    }

    @GetMapping("/partido/{partidoId}/promedio")
    public ResponseEntity<Double> obtenerPromedioPorPartido(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long partidoId) {
        Double promedio = calificacionService.obtenerPromedioPorPartido(partidoId);
        return ResponseEntity.ok(promedio);
    }

    @GetMapping("/creador/{creadorNombre}/promedio")
    public ResponseEntity<Double> obtenerPromedioPorCreador(
            @PathVariable @NonNull String creadorNombre) {
        Double promedio = calificacionService.obtenerPromedioPorCreador(creadorNombre);
        return ResponseEntity.ok(promedio);
    }

    @GetMapping("/sede/{sedeId}/promedio")
    public ResponseEntity<Double> obtenerPromedioPorSede(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long sedeId) {
        Double promedio = calificacionService.obtenerPromedioPorSede(sedeId);
        return ResponseEntity.ok(promedio);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalificacionResponseDTO> obtenerPorId(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        CalificacionResponseDTO calificacion = calificacionService.obtenerPorId(id);
        return ResponseEntity.ok(calificacion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        calificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

