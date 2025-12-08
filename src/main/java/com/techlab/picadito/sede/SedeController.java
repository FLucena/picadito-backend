package com.techlab.picadito.sede;

import com.techlab.picadito.dto.SedeDTO;
import com.techlab.picadito.dto.SedeResponseDTO;
import com.techlab.picadito.dto.SedesResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sedes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class SedeController {

    @Autowired
    private SedeService sedeService;

    @GetMapping
    public ResponseEntity<SedesResponseDTO> obtenerTodas() {
        SedesResponseDTO sedes = sedeService.obtenerTodas();
        return ResponseEntity.ok(sedes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SedeResponseDTO> obtenerPorId(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        SedeResponseDTO sede = sedeService.obtenerPorId(id);
        return ResponseEntity.ok(sede);
    }

    @PostMapping
    public ResponseEntity<SedeResponseDTO> crear(@Valid @RequestBody SedeDTO dto) {
        SedeResponseDTO sede = sedeService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(sede);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SedeResponseDTO> actualizar(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id,
            @Valid @RequestBody SedeDTO dto) {
        SedeResponseDTO sede = sedeService.actualizar(id, dto);
        return ResponseEntity.ok(sede);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El ID debe ser un número positivo") @NonNull Long id) {
        sedeService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/migrar")
    public ResponseEntity<Map<String, Object>> migrar() {
        Map<String, Object> resultado = sedeService.migrarUbicacionesASedes();
        return ResponseEntity.ok(resultado);
    }
}

