package com.techlab.picadito.partido;

import com.techlab.picadito.dto.BusquedaPartidoDTO;
import com.techlab.picadito.dto.PageResponseDTO;
import com.techlab.picadito.dto.PartidoDTO;
import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.dto.PartidosResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<PageResponseDTO<PartidoResponseDTO>> obtenerTodosLosPartidos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHora") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        PageResponseDTO<PartidoResponseDTO> partidos = partidoService.obtenerTodosLosPartidos(pageable);
        return ResponseEntity.ok(partidos);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<PageResponseDTO<PartidoResponseDTO>> obtenerPartidosDisponibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHora") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        PageResponseDTO<PartidoResponseDTO> partidos = partidoService.obtenerPartidosDisponibles(pageable);
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
    public ResponseEntity<PageResponseDTO<PartidoResponseDTO>> buscarPartidos(
            @RequestBody BusquedaPartidoDTO busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHora") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        PageResponseDTO<PartidoResponseDTO> partidos = partidoService.buscarPartidos(busqueda, pageable);
        return ResponseEntity.ok(partidos);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<PartidosResponseDTO> obtenerPartidosPorCategoria(
            @PathVariable String categoriaId) {
        try {
            Long idLong = Long.parseLong(categoriaId);
            BusquedaPartidoDTO busqueda = new BusquedaPartidoDTO();
            busqueda.setCategoriaIds(List.of(idLong));
            PartidosResponseDTO partidos = partidoService.buscarPartidos(busqueda);
            return ResponseEntity.ok(partidos);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

