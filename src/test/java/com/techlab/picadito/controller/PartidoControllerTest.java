package com.techlab.picadito.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.picadito.dto.PartidoDTO;
import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.service.PartidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartidoController.class)
class PartidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private PartidoService partidoService;

    @Autowired
    private ObjectMapper objectMapper;

    private PartidoResponseDTO partidoResponse;
    private PartidoDTO partidoDTO;

    @BeforeEach
    void setUp() {
        partidoResponse = new PartidoResponseDTO();
        partidoResponse.setId(1L);
        partidoResponse.setTitulo("Partido de Prueba");
        partidoResponse.setDescripcion("Descripción del partido");
        partidoResponse.setFechaHora(LocalDateTime.now().plusDays(1));
        partidoResponse.setMaxJugadores(10);
        partidoResponse.setCantidadParticipantes(5);
        partidoResponse.setEstado(EstadoPartido.DISPONIBLE);
        partidoResponse.setCreadorNombre("Test User");

        partidoDTO = new PartidoDTO();
        partidoDTO.setTitulo("Partido de Prueba");
        partidoDTO.setDescripcion("Descripción del partido");
        partidoDTO.setFechaHora(LocalDateTime.now().plusDays(1));
        partidoDTO.setMaxJugadores(10);
        partidoDTO.setCreadorNombre("Test User");
    }

    @Test
    void obtenerTodosLosPartidos_ShouldReturnListOfPartidos() throws Exception {
        List<PartidoResponseDTO> partidos = Arrays.asList(partidoResponse);
        when(partidoService.obtenerTodosLosPartidos()).thenReturn(partidos);

        mockMvc.perform(get("/api/partidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Partido de Prueba"));
    }

    @Test
    void obtenerPartidosDisponibles_ShouldReturnAvailablePartidos() throws Exception {
        List<PartidoResponseDTO> partidos = Arrays.asList(partidoResponse);
        when(partidoService.obtenerPartidosDisponibles()).thenReturn(partidos);

        mockMvc.perform(get("/api/partidos/disponibles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPartidoPorId_WithValidId_ShouldReturnPartido() throws Exception {
        when(partidoService.obtenerPartidoPorId(1L)).thenReturn(partidoResponse);

        mockMvc.perform(get("/api/partidos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Partido de Prueba"));
    }

    @Test
    void obtenerPartidoPorId_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/partidos/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SuppressWarnings("null")
    void crearPartido_WithValidData_ShouldReturnCreated() throws Exception {
        when(partidoService.crearPartido(any(PartidoDTO.class))).thenReturn(partidoResponse);

        String jsonContent = objectMapper.writeValueAsString(partidoDTO);
        mockMvc.perform(post("/api/partidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Partido de Prueba"));
    }

    @Test
    @SuppressWarnings("null")
    void actualizarPartido_WithValidData_ShouldReturnUpdatedPartido() throws Exception {
        when(partidoService.actualizarPartido(eq(1L), any(PartidoDTO.class))).thenReturn(partidoResponse);

        String jsonContent = objectMapper.writeValueAsString(partidoDTO);
        mockMvc.perform(put("/api/partidos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminarPartido_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/partidos/1"))
                .andExpect(status().isNoContent());
    }
}

