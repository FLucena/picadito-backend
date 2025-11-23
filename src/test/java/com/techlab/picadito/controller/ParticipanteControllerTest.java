package com.techlab.picadito.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.picadito.dto.ParticipanteDTO;
import com.techlab.picadito.dto.ParticipanteResponseDTO;
import com.techlab.picadito.model.Nivel;
import com.techlab.picadito.model.Posicion;
import com.techlab.picadito.service.ParticipanteService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParticipanteController.class)
class ParticipanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @MockBean
    private ParticipanteService participanteService;

    @Autowired
    private ObjectMapper objectMapper;

    private ParticipanteResponseDTO participanteResponse;
    private ParticipanteDTO participanteDTO;

    @BeforeEach
    void setUp() {
        participanteResponse = new ParticipanteResponseDTO();
        participanteResponse.setId(1L);
        participanteResponse.setNombre("Juan Pérez");
        participanteResponse.setApodo("Juancho");
        participanteResponse.setPosicion(Posicion.DELANTERO);
        participanteResponse.setNivel(Nivel.INTERMEDIO);
        participanteResponse.setFechaInscripcion(LocalDateTime.now());

        participanteDTO = new ParticipanteDTO();
        participanteDTO.setNombre("Juan Pérez");
        participanteDTO.setApodo("Juancho");
        participanteDTO.setPosicion(Posicion.DELANTERO);
        participanteDTO.setNivel(Nivel.INTERMEDIO);
    }

    @Test
    void inscribirseAPartido_WithValidData_ShouldReturnCreated() throws Exception {
        when(participanteService.inscribirseAPartido(eq(1L), any(ParticipanteDTO.class)))
                .thenReturn(participanteResponse);

        String jsonContent = objectMapper.writeValueAsString(participanteDTO);
        mockMvc.perform(post("/api/partidos/1/participantes")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));
    }

    @Test
    void inscribirseAPartido_WithInvalidPartidoId_ShouldReturnInternalServerError() throws Exception {
        String jsonContent = objectMapper.writeValueAsString(participanteDTO);
        // Spring devuelve 500 cuando no puede convertir el path variable a Long
        mockMvc.perform(post("/api/partidos/invalid/participantes")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonContent))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void obtenerParticipantesPorPartido_WithValidId_ShouldReturnList() throws Exception {
        List<ParticipanteResponseDTO> participantes = Arrays.asList(participanteResponse);
        when(participanteService.obtenerParticipantesPorPartido(1L)).thenReturn(participantes);

        mockMvc.perform(get("/api/partidos/1/participantes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"));
    }

    @Test
    void obtenerParticipantesPorPartido_WithInvalidId_ShouldReturnInternalServerError() throws Exception {
        // Spring devuelve 500 cuando no puede convertir el path variable a Long
        mockMvc.perform(get("/api/partidos/invalid/participantes"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void desinscribirseDePartido_WithValidIds_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/partidos/1/participantes/1"))
                .andExpect(status().isNoContent());

        verify(participanteService, times(1)).desinscribirseDePartido(1L, 1L);
    }

    @Test
    void desinscribirseDePartido_WithInvalidIds_ShouldReturnInternalServerError() throws Exception {
        // Spring devuelve 500 cuando no puede convertir el path variable a Long
        mockMvc.perform(delete("/api/partidos/invalid/participantes/1"))
                .andExpect(status().isInternalServerError());

        mockMvc.perform(delete("/api/partidos/1/participantes/invalid"))
                .andExpect(status().isInternalServerError());
    }
}

