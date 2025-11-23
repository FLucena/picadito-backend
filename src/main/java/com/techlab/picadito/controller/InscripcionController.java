package com.techlab.picadito.controller;

import com.techlab.picadito.dto.InscripcionConfirmadaResponseDTO;
import com.techlab.picadito.dto.ParticipanteDTO;
import com.techlab.picadito.service.InscripcionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @PostMapping("/usuario/{usuarioId}/confirmar")
    public ResponseEntity<InscripcionConfirmadaResponseDTO> confirmarInscripcionesDesdePartidosGuardados(
            @PathVariable @Positive(message = "El ID del usuario debe ser un número positivo") @NonNull Long usuarioId,
            @Valid @RequestBody ParticipanteDTO participanteDTO) {
        InscripcionConfirmadaResponseDTO inscripcion = inscripcionService.confirmarInscripcionesDesdePartidosGuardados(usuarioId, participanteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(inscripcion);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<InscripcionConfirmadaResponseDTO>> obtenerInscripcionesPorUsuario(
            @PathVariable @Positive(message = "El ID del usuario debe ser un número positivo") @NonNull Long usuarioId) {
        List<InscripcionConfirmadaResponseDTO> inscripciones = inscripcionService.obtenerInscripcionesPorUsuario(usuarioId);
        return ResponseEntity.ok(inscripciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscripcionConfirmadaResponseDTO> obtenerInscripcionPorId(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        InscripcionConfirmadaResponseDTO inscripcion = inscripcionService.obtenerInscripcionPorId(id);
        return ResponseEntity.ok(inscripcion);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarInscripcion(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        inscripcionService.cancelarInscripcion(id);
        return ResponseEntity.noContent().build();
    }
}

