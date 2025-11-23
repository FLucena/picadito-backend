package com.techlab.picadito.controller;

import com.techlab.picadito.dto.PartidosSeleccionadosDTO;
import com.techlab.picadito.service.PartidosSeleccionadosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partidos-seleccionados")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
@RequiredArgsConstructor
public class PartidosSeleccionadosController {
    
    private final PartidosSeleccionadosService partidosSeleccionadosService;
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PartidosSeleccionadosDTO> obtenerPartidosSeleccionados(@PathVariable String usuarioId) {
        try {
            Long id = Long.parseLong(usuarioId);
            return ResponseEntity.ok(partidosSeleccionadosService.obtenerPartidosSeleccionadosPorUsuario(id));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/usuario/{usuarioId}/agregar")
    public ResponseEntity<PartidosSeleccionadosDTO> agregarPartido(
            @PathVariable String usuarioId,
            @RequestParam Long partidoId,
            @RequestParam(defaultValue = "1") Integer cantidad) {
        try {
            Long idUsuario = Long.parseLong(usuarioId);
            return ResponseEntity.ok(partidosSeleccionadosService.agregarPartido(idUsuario, partidoId, cantidad));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/usuario/{usuarioId}/item/{lineaPartidoSeleccionadoId}")
    public ResponseEntity<PartidosSeleccionadosDTO> actualizarCantidad(
            @PathVariable String usuarioId,
            @PathVariable String lineaPartidoSeleccionadoId,
            @RequestParam Integer cantidad) {
        try {
            Long idUsuario = Long.parseLong(usuarioId);
            Long idLinea = Long.parseLong(lineaPartidoSeleccionadoId);
            return ResponseEntity.ok(partidosSeleccionadosService.actualizarCantidad(idUsuario, idLinea, cantidad));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/usuario/{usuarioId}/item/{lineaPartidoSeleccionadoId}")
    public ResponseEntity<Void> eliminarItem(
            @PathVariable String usuarioId,
            @PathVariable String lineaPartidoSeleccionadoId) {
        try {
            Long idUsuario = Long.parseLong(usuarioId);
            Long idLinea = Long.parseLong(lineaPartidoSeleccionadoId);
            partidosSeleccionadosService.eliminarItem(idUsuario, idLinea);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Void> vaciarPartidosSeleccionados(@PathVariable String usuarioId) {
        try {
            Long id = Long.parseLong(usuarioId);
            partidosSeleccionadosService.vaciarPartidosSeleccionados(id);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

