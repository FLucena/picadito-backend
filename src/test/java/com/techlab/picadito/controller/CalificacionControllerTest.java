package com.techlab.picadito.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.picadito.dto.CalificacionDTO;
import com.techlab.picadito.dto.CalificacionResponseDTO;
import com.techlab.picadito.dto.CalificacionesResponseDTO;
import com.techlab.picadito.calificacion.CalificacionService;
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

@WebMvcTest(com.techlab.picadito.calificacion.CalificacionController.class)
class CalificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private CalificacionService calificacionService;

    @Autowired
    private ObjectMapper objectMapper;

    private CalificacionResponseDTO calificacionResponse;
    private CalificacionDTO calificacionDTO;

    @BeforeEach
    void setUp() {
        calificacionResponse = new CalificacionResponseDTO();
        calificacionResponse.setId(1L);
        calificacionResponse.setPuntuacion(5);
        calificacionResponse.setComentario("Excelente partido");
        calificacionResponse.setUsuarioId(1L);
        calificacionResponse.setUsuarioNombre("Test User");
        calificacionResponse.setPartidoId(1L);
        calificacionResponse.setPartidoTitulo("Partido de Prueba");
        calificacionResponse.setFechaCreacion(LocalDateTime.now());

        calificacionDTO = new CalificacionDTO();
        calificacionDTO.setPuntuacion(5);
        calificacionDTO.setComentario("Excelente partido");
        calificacionDTO.setPartidoId(1L);
    }

    @Test
    @SuppressWarnings("null")
    void crear_WithValidData_ShouldReturnCreated() throws Exception {
        when(calificacionService.crear(eq(1L), any(CalificacionDTO.class))).thenReturn(calificacionResponse);

        String jsonContent = objectMapper.writeValueAsString(calificacionDTO);
        mockMvc.perform(post("/api/calificaciones/usuario/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.puntuacion").value(5));
    }

    @Test
    void obtenerPorPartido_ShouldReturnListOfCalificaciones() throws Exception {
        List<CalificacionResponseDTO> calificaciones = Arrays.asList(calificacionResponse);
        CalificacionesResponseDTO calificacionesResponse = new CalificacionesResponseDTO(calificaciones);
        when(calificacionService.obtenerPorPartido(1L)).thenReturn(calificacionesResponse);

        mockMvc.perform(get("/api/calificaciones/partido/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.calificaciones[0].id").value(1))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void obtenerPromedioPorPartido_ShouldReturnAverage() throws Exception {
        when(calificacionService.obtenerPromedioPorPartido(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/calificaciones/partido/1/promedio"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").value(4.5));
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnCalificacion() throws Exception {
        when(calificacionService.obtenerPorId(1L)).thenReturn(calificacionResponse);

        mockMvc.perform(get("/api/calificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminar_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/calificaciones/1"))
                .andExpect(status().isNoContent());
    }
}


