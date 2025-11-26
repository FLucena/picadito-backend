package com.techlab.picadito.controller;

import com.techlab.picadito.dto.BusquedaPartidoDTO;
import com.techlab.picadito.dto.PartidoDTO;
import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.service.PartidoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partidos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
public class PartidoController {

    @Autowired
    private PartidoService partidoService;

    @GetMapping
    public ResponseEntity<List<PartidoResponseDTO>> obtenerTodosLosPartidos() {
        List<PartidoResponseDTO> partidos = partidoService.obtenerTodosLosPartidos();
        return ResponseEntity.ok(partidos);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<PartidoResponseDTO>> obtenerPartidosDisponibles() {
        List<PartidoResponseDTO> partidos = partidoService.obtenerPartidosDisponibles();
        return ResponseEntity.ok(partidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidoResponseDTO> obtenerPartidoPorId(
            @PathVariable String id) {
        try {
            Long idLong = Long.parseLong(id);
            PartidoResponseDTO partido = partidoService.obtenerPartidoPorId(idLong);
            return ResponseEntity.ok(partido);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/costo-por-jugador")
    public ResponseEntity<Double> obtenerCostoPorJugador(@PathVariable String id) {
        try {
            Long idLong = Long.parseLong(id);
            Double costo = partidoService.calcularCostoPorJugador(idLong);
            if (costo == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(costo);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<PartidoResponseDTO> crearPartido(@Valid @RequestBody PartidoDTO partidoDTO) {
        PartidoResponseDTO partido = partidoService.crearPartido(partidoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(partido);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartidoResponseDTO> actualizarPartido(
            @PathVariable String id, 
            @Valid @RequestBody PartidoDTO partidoDTO) {
        try {
            Long idLong = Long.parseLong(id);
            PartidoResponseDTO partido = partidoService.actualizarPartido(idLong, partidoDTO);
            return ResponseEntity.ok(partido);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPartido(@PathVariable String id) {
        try {
            Long idLong = Long.parseLong(id);
            partidoService.eliminarPartido(idLong);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/buscar")
    public ResponseEntity<List<PartidoResponseDTO>> buscarPartidos(@RequestBody BusquedaPartidoDTO busqueda) {
        List<PartidoResponseDTO> partidos = partidoService.buscarPartidos(busqueda);
        return ResponseEntity.ok(partidos);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<PartidoResponseDTO>> obtenerPartidosPorCategoria(
            @PathVariable String categoriaId) {
        try {
            Long idLong = Long.parseLong(categoriaId);
            BusquedaPartidoDTO busqueda = new BusquedaPartidoDTO();
            busqueda.setCategoriaIds(List.of(idLong));
            List<PartidoResponseDTO> partidos = partidoService.buscarPartidos(busqueda);
            return ResponseEntity.ok(partidos);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

