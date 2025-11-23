package com.techlab.picadito.controller;

import com.techlab.picadito.dto.PartidosGuardadosResponseDTO;
import com.techlab.picadito.service.PartidosGuardadosService;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partidos-guardados")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class PartidosGuardadosController {

    @Autowired
    private PartidosGuardadosService partidosGuardadosService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PartidosGuardadosResponseDTO> obtenerPartidosGuardadosPorUsuario(
            @PathVariable @Positive(message = "El ID del usuario debe ser un número positivo") @NonNull Long usuarioId) {
        PartidosGuardadosResponseDTO partidosGuardados = partidosGuardadosService.obtenerPartidosGuardadosPorUsuario(usuarioId);
        return ResponseEntity.ok(partidosGuardados);
    }

    @PostMapping("/usuario/{usuarioId}/agregar")
    public ResponseEntity<PartidosGuardadosResponseDTO> agregarPartido(
            @PathVariable @Positive(message = "El ID del usuario debe ser un número positivo") @NonNull Long usuarioId,
            @RequestParam @Positive(message = "El ID del partido debe ser un número positivo") @NonNull Long partidoId) {
        PartidosGuardadosResponseDTO partidosGuardados = partidosGuardadosService.agregarPartido(usuarioId, partidoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(partidosGuardados);
    }

    @DeleteMapping("/usuario/{usuarioId}/partido/{lineaPartidoGuardadoId}")
    public ResponseEntity<Void> eliminarPartido(
            @PathVariable @Positive(message = "El ID del usuario debe ser un número positivo") @NonNull Long usuarioId,
            @PathVariable @Positive(message = "El ID de la línea debe ser un número positivo") @NonNull Long lineaPartidoGuardadoId) {
        partidosGuardadosService.eliminarPartido(usuarioId, lineaPartidoGuardadoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Void> vaciarPartidosGuardados(
            @PathVariable @Positive(message = "El ID del usuario debe ser un número positivo") @NonNull Long usuarioId) {
        partidosGuardadosService.vaciarPartidosGuardados(usuarioId);
        return ResponseEntity.noContent().build();
    }
}

