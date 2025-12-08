package com.techlab.picadito.controller;

import com.techlab.picadito.dto.EstadisticasDTO;
import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.dto.PartidosResponseDTO;
import com.techlab.picadito.dto.ReporteDTO;
import com.techlab.picadito.admin.AdminService;
import com.techlab.picadito.service.EstadisticasService;
import com.techlab.picadito.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.techlab.picadito.admin.AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private AdminService adminService;

    @MockBean
    @SuppressWarnings("removal")
    private EstadisticasService estadisticasService;

    @MockBean
    @SuppressWarnings("removal")
    private ReporteService reporteService;

    private EstadisticasDTO estadisticasDTO;
    private ReporteDTO reporteDTO;

    @BeforeEach
    void setUp() {
        estadisticasDTO = new EstadisticasDTO();
        estadisticasDTO.setTotalPartidos(10L);
        estadisticasDTO.setTotalReservas(25L);
        estadisticasDTO.setTotalUsuarios(5L);
        estadisticasDTO.setIngresosTotales(5000.0);

        reporteDTO = new ReporteDTO();
        reporteDTO.setTipoReporte("VENTAS");
        reporteDTO.setFechaInicio(LocalDateTime.now().minusDays(30));
        reporteDTO.setFechaFin(LocalDateTime.now());
        reporteDTO.setFechaGeneracion(LocalDateTime.now());
        Map<String, Object> datos = new HashMap<>();
        datos.put("totalReservas", 25);
        datos.put("ingresosTotales", 5000.0);
        reporteDTO.setDatos(datos);
    }

    @Test
    void obtenerPartidosConCapacidadBaja_ShouldReturnListOfPartidos() throws Exception {
        PartidoResponseDTO partido = new PartidoResponseDTO();
        partido.setId(1L);
        partido.setTitulo("Partido con pocos cupos");
        List<PartidoResponseDTO> partidos = Arrays.asList(partido);
        PartidosResponseDTO partidosResponse = new PartidosResponseDTO(partidos);
        when(adminService.obtenerPartidosConCapacidadBaja(any())).thenReturn(partidosResponse);

        mockMvc.perform(get("/api/admin/partidos-capacidad-baja"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.partidos[0].id").value(1))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void obtenerEstadisticas_ShouldReturnEstadisticas() throws Exception {
        when(estadisticasService.obtenerEstadisticasGenerales()).thenReturn(estadisticasDTO);

        mockMvc.perform(get("/api/admin/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.totalPartidos").value(10))
                .andExpect(jsonPath("$.totalReservas").value(25));
    }

    @Test
    void generarReporteVentas_ShouldReturnReporte() throws Exception {
        when(reporteService.generarReporteVentas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(reporteDTO);

        mockMvc.perform(get("/api/admin/reportes/ventas")
                        .param("fechaInicio", LocalDateTime.now().minusDays(30).toString())
                        .param("fechaFin", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.tipoReporte").value("VENTAS"));
    }
}


