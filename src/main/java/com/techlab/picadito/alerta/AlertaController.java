package com.techlab.picadito.alerta;

import com.techlab.picadito.dto.AlertaDTO;
import com.techlab.picadito.dto.AlertaResponseDTO;
import com.techlab.picadito.dto.AlertasResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<AlertasResponseDTO> obtenerPorUsuario(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long usuarioId) {
        AlertasResponseDTO alertas = alertaService.obtenerPorUsuario(usuarioId);
        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<AlertasResponseDTO> obtenerNoLeidasPorUsuario(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long usuarioId) {
        AlertasResponseDTO alertas = alertaService.obtenerNoLeidasPorUsuario(usuarioId);
        return ResponseEntity.ok(alertas);
    }

    @PostMapping
    public ResponseEntity<AlertaResponseDTO> crear(@Valid @RequestBody AlertaDTO dto) {
        AlertaResponseDTO alerta = alertaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(alerta);
    }

    @PutMapping("/{id}/marcar-leida")
    public ResponseEntity<AlertaResponseDTO> marcarComoLeida(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        AlertaResponseDTO alerta = alertaService.marcarComoLeida(id);
        return ResponseEntity.ok(alerta);
    }

    @PutMapping("/usuario/{usuarioId}/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasComoLeidas(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long usuarioId) {
        alertaService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        alertaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

