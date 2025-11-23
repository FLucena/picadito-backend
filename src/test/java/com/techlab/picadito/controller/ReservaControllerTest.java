package com.techlab.picadito.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.picadito.dto.ReservaDTO;
import com.techlab.picadito.model.Reserva;
import com.techlab.picadito.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.verify;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @MockBean
    private ReservaService reservaService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReservaDTO reservaDTO;

    @BeforeEach
    void setUp() {
        reservaDTO = new ReservaDTO();
        reservaDTO.setId(1L);
        reservaDTO.setUsuarioId(1L);
        reservaDTO.setEstado(Reserva.EstadoReserva.PENDIENTE);
        reservaDTO.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void obtenerTodas_ShouldReturnListOfReservas() throws Exception {
        List<ReservaDTO> reservas = Arrays.asList(reservaDTO);
        when(reservaService.obtenerTodos()).thenReturn(reservas);

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].usuarioId").value(1));
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnReserva() throws Exception {
        when(reservaService.obtenerPorId(1L)).thenReturn(reservaDTO);

        mockMvc.perform(get("/api/reservas/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1));
    }

    @Test
    void obtenerPorId_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/reservas/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPorUsuario_WithValidId_ShouldReturnUserReservas() throws Exception {
        List<ReservaDTO> reservas = Arrays.asList(reservaDTO);
        when(reservaService.obtenerPorUsuario(1L)).thenReturn(reservas);

        mockMvc.perform(get("/api/reservas/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPorUsuario_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/reservas/usuario/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTotalGastado_WithValidId_ShouldReturnTotal() throws Exception {
        when(reservaService.calcularTotalGastadoPorUsuario(1L)).thenReturn(150.0);

        mockMvc.perform(get("/api/reservas/usuario/1/total-gastado"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").value(150.0));
    }

    @Test
    void obtenerTotalGastado_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/reservas/usuario/invalid/total-gastado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearDesdePartidosSeleccionados_WithValidId_ShouldReturnCreated() throws Exception {
        reservaDTO.setEstado(Reserva.EstadoReserva.CONFIRMADO);
        when(reservaService.crearDesdePartidosSeleccionados(1L)).thenReturn(reservaDTO);

        mockMvc.perform(post("/api/reservas/desde-partidos-seleccionados/1"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearDesdePartidosSeleccionados_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/reservas/desde-partidos-seleccionados/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarEstado_WithValidData_ShouldReturnUpdatedReserva() throws Exception {
        reservaDTO.setEstado(Reserva.EstadoReserva.CONFIRMADO);
        when(reservaService.actualizarEstado(eq(1L), any(Reserva.EstadoReserva.class)))
                .thenReturn(reservaDTO);

        mockMvc.perform(put("/api/reservas/1/estado")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("\"CONFIRMADO\""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarEstado_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/reservas/invalid/estado")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("\"CONFIRMADO\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelar_WithValidId_ShouldReturnOk() throws Exception {
        mockMvc.perform(put("/api/reservas/1/cancelar"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelar_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/reservas/invalid/cancelar"))
                .andExpect(status().isBadRequest());
    }
}

