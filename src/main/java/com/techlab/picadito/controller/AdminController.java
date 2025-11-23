package com.techlab.picadito.controller;

import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:5173"})
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    
    /**
     * Obtiene partidos con capacidad disponible baja (equivalente a stock bajo)
     * 
     * @param capacidadMinima Capacidad mínima disponible para considerar un partido como crítico (opcional, default: 5)
     * @return Lista de partidos con capacidad baja, ordenados por capacidad disponible ascendente
     */
    @GetMapping("/partidos-capacidad-baja")
    public ResponseEntity<List<PartidoResponseDTO>> obtenerPartidosConCapacidadBaja(
            @RequestParam(required = false) Integer capacidadMinima) {
        List<PartidoResponseDTO> partidos = adminService.obtenerPartidosConCapacidadBaja(capacidadMinima);
        return ResponseEntity.ok(partidos);
    }
}

