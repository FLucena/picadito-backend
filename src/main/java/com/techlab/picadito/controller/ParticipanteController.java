package com.techlab.picadito.controller;

import com.techlab.picadito.dto.ParticipanteDTO;
import com.techlab.picadito.dto.ParticipanteResponseDTO;
import com.techlab.picadito.service.ParticipanteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partidos/{partidoId}/participantes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class ParticipanteController {

    @Autowired
    private ParticipanteService participanteService;

    @PostMapping
    public ResponseEntity<ParticipanteResponseDTO> inscribirseAPartido(
            @PathVariable @Positive(message = "El ID del partido debe ser un número positivo") @NonNull Long partidoId, 
            @Valid @RequestBody ParticipanteDTO participanteDTO) {
        ParticipanteResponseDTO participante = participanteService.inscribirseAPartido(partidoId, participanteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(participante);
    }

    @GetMapping
    public ResponseEntity<List<ParticipanteResponseDTO>> obtenerParticipantesPorPartido(
            @PathVariable @Positive(message = "El ID del partido debe ser un número positivo") @NonNull Long partidoId) {
        List<ParticipanteResponseDTO> participantes = participanteService.obtenerParticipantesPorPartido(partidoId);
        return ResponseEntity.ok(participantes);
    }

    @DeleteMapping("/{participanteId}")
    public ResponseEntity<Void> desinscribirseDePartido(
            @PathVariable @Positive(message = "El ID del partido debe ser un número positivo") @NonNull Long partidoId, 
            @PathVariable @Positive(message = "El ID del participante debe ser un número positivo") @NonNull Long participanteId) {
        participanteService.desinscribirseDePartido(partidoId, participanteId);
        return ResponseEntity.noContent().build();
    }
}

