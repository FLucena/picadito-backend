package com.techlab.picadito.controller;

import com.techlab.picadito.dto.ReservaDTO;
import com.techlab.picadito.model.Reserva;
import com.techlab.picadito.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
@RequiredArgsConstructor
public class ReservaController {
    
    private final ReservaService reservaService;
    
    @GetMapping
    public ResponseEntity<List<ReservaDTO>> obtenerTodas() {
        return ResponseEntity.ok(reservaService.obtenerTodos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> obtenerPorId(@PathVariable String id) {
        try {
            Long idLong = Long.parseLong(id);
            return ResponseEntity.ok(reservaService.obtenerPorId(idLong));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaDTO>> obtenerPorUsuario(@PathVariable String usuarioId) {
        try {
            Long id = Long.parseLong(usuarioId);
            return ResponseEntity.ok(reservaService.obtenerPorUsuario(id));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/usuario/{usuarioId}/total-gastado")
    public ResponseEntity<Double> obtenerTotalGastado(@PathVariable String usuarioId) {
        try {
            Long id = Long.parseLong(usuarioId);
            Double total = reservaService.calcularTotalGastadoPorUsuario(id);
            return ResponseEntity.ok(total);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/desde-partidos-seleccionados/{usuarioId}")
    public ResponseEntity<ReservaDTO> crearDesdePartidosSeleccionados(@PathVariable String usuarioId) {
        try {
            Long id = Long.parseLong(usuarioId);
            return new ResponseEntity<>(reservaService.crearDesdePartidosSeleccionados(id), HttpStatus.CREATED);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/estado")
    public ResponseEntity<ReservaDTO> actualizarEstado(
            @PathVariable String id,
            @RequestBody Reserva.EstadoReserva estado) {
        try {
            Long idLong = Long.parseLong(id);
            return ResponseEntity.ok(reservaService.actualizarEstado(idLong, estado));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        try {
            Long idLong = Long.parseLong(id);
            reservaService.cancelar(idLong);
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

