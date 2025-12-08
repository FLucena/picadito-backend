package com.techlab.picadito.admin;

import com.techlab.picadito.dto.EstadisticasDTO;
import com.techlab.picadito.dto.PartidosResponseDTO;
import com.techlab.picadito.dto.ReporteDTO;
import com.techlab.picadito.service.EstadisticasService;
import com.techlab.picadito.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    private final EstadisticasService estadisticasService;
    private final ReporteService reporteService;
    
    /**
     * Obtiene partidos con capacidad disponible baja (equivalente a stock bajo)
     * 
     * @param capacidadMinima Capacidad mínima disponible para considerar un partido como crítico (opcional, default: 5)
     * @return Lista de partidos con capacidad baja, ordenados por capacidad disponible ascendente
     */
    @GetMapping("/partidos-capacidad-baja")
    public ResponseEntity<PartidosResponseDTO> obtenerPartidosConCapacidadBaja(
            @RequestParam(required = false) Integer capacidadMinima) {
        PartidosResponseDTO partidos = adminService.obtenerPartidosConCapacidadBaja(capacidadMinima);
        return ResponseEntity.ok(partidos);
    }

    /**
     * Obtiene estadísticas generales del sistema
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticas() {
        EstadisticasDTO estadisticas = estadisticasService.obtenerEstadisticasGenerales();
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene estadísticas para un período específico
     */
    @GetMapping("/estadisticas/periodo")
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        EstadisticasDTO estadisticas = estadisticasService.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Genera reporte de ventas para un período específico
     */
    @GetMapping("/reportes/ventas")
    public ResponseEntity<ReporteDTO> generarReporteVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        ReporteDTO reporte = reporteService.generarReporteVentas(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    /**
     * Genera reporte de partidos para un período específico
     */
    @GetMapping("/reportes/partidos")
    public ResponseEntity<ReporteDTO> generarReportePartidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        ReporteDTO reporte = reporteService.generarReportePartidos(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    /**
     * Genera reporte de usuarios para un período específico
     */
    @GetMapping("/reportes/usuarios")
    public ResponseEntity<ReporteDTO> generarReporteUsuarios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        ReporteDTO reporte = reporteService.generarReporteUsuarios(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
}

