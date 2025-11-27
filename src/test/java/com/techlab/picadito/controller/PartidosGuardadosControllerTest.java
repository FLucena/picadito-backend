package com.techlab.picadito.controller;

import com.techlab.picadito.dto.PartidosGuardadosResponseDTO;
import com.techlab.picadito.service.PartidosGuardadosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartidosGuardadosController.class)
class PartidosGuardadosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private PartidosGuardadosService partidosGuardadosService;

    private PartidosGuardadosResponseDTO partidosGuardadosResponse;

    @BeforeEach
    void setUp() {
        partidosGuardadosResponse = new PartidosGuardadosResponseDTO();
        partidosGuardadosResponse.setId(1L);
        partidosGuardadosResponse.setUsuarioId(1L);
        partidosGuardadosResponse.setUsuarioNombre("Test User");
        partidosGuardadosResponse.setTotalPartidos(0);
        partidosGuardadosResponse.setPartidos(new ArrayList<>());
        partidosGuardadosResponse.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void obtenerPartidosGuardadosPorUsuario_WithValidId_ShouldReturnPartidosGuardados() throws Exception {
        when(partidosGuardadosService.obtenerPartidosGuardadosPorUsuario(1L))
                .thenReturn(partidosGuardadosResponse);

        mockMvc.perform(get("/api/partidos-guardados/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1));
    }

    @Test
    void obtenerPartidosGuardadosPorUsuario_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/partidos-guardados/usuario/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agregarPartido_WithValidIds_ShouldReturnCreated() throws Exception {
        when(partidosGuardadosService.agregarPartido(eq(1L), eq(1L)))
                .thenReturn(partidosGuardadosResponse);

        mockMvc.perform(post("/api/partidos-guardados/usuario/1/agregar")
                        .param("partidoId", "1"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void agregarPartido_WithInvalidIds_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/partidos-guardados/usuario/invalid/agregar")
                        .param("partidoId", "1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/partidos-guardados/usuario/1/agregar")
                        .param("partidoId", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarPartido_WithValidIds_ShouldReturnNoContent() throws Exception {
        when(partidosGuardadosService.eliminarPartido(eq(1L), eq(1L)))
                .thenReturn(partidosGuardadosResponse);

        mockMvc.perform(delete("/api/partidos-guardados/usuario/1/partido/1"))
                .andExpect(status().isNoContent());

        verify(partidosGuardadosService).eliminarPartido(1L, 1L);
    }

    @Test
    void eliminarPartido_WithInvalidIds_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/partidos-guardados/usuario/invalid/partido/1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/partidos-guardados/usuario/1/partido/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vaciarPartidosGuardados_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/partidos-guardados/usuario/1"))
                .andExpect(status().isNoContent());

        verify(partidosGuardadosService).vaciarPartidosGuardados(1L);
    }

    @Test
    void vaciarPartidosGuardados_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/partidos-guardados/usuario/invalid"))
                .andExpect(status().isBadRequest());
    }
}

