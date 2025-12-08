package com.techlab.picadito.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.picadito.dto.SedeDTO;
import com.techlab.picadito.dto.SedeResponseDTO;
import com.techlab.picadito.dto.SedesResponseDTO;
import com.techlab.picadito.sede.SedeService;
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

@WebMvcTest(com.techlab.picadito.sede.SedeController.class)
class SedeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private SedeService sedeService;

    @Autowired
    private ObjectMapper objectMapper;

    private SedeResponseDTO sedeResponse;
    private SedeDTO sedeDTO;

    @BeforeEach
    void setUp() {
        sedeResponse = new SedeResponseDTO();
        sedeResponse.setId(1L);
        sedeResponse.setNombre("Estadio Olímpico");
        sedeResponse.setDireccion("Av. Principal 123");
        sedeResponse.setDescripcion("Estadio moderno");
        sedeResponse.setTelefono("1234567890");
        sedeResponse.setFechaCreacion(LocalDateTime.now());

        sedeDTO = new SedeDTO();
        sedeDTO.setNombre("Estadio Olímpico");
        sedeDTO.setDireccion("Av. Principal 123");
        sedeDTO.setDescripcion("Estadio moderno");
        sedeDTO.setTelefono("1234567890");
    }

    @Test
    void obtenerTodas_ShouldReturnListOfSedes() throws Exception {
        List<SedeResponseDTO> sedes = Arrays.asList(sedeResponse);
        SedesResponseDTO sedesResponse = new SedesResponseDTO(sedes);
        when(sedeService.obtenerTodas()).thenReturn(sedesResponse);

        mockMvc.perform(get("/api/sedes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.sedes[0].id").value(1))
                .andExpect(jsonPath("$.sedes[0].nombre").value("Estadio Olímpico"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnSede() throws Exception {
        when(sedeService.obtenerPorId(1L)).thenReturn(sedeResponse);

        mockMvc.perform(get("/api/sedes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Estadio Olímpico"));
    }

    @Test
    @SuppressWarnings("null")
    void crear_WithValidData_ShouldReturnCreated() throws Exception {
        when(sedeService.crear(any(SedeDTO.class))).thenReturn(sedeResponse);

        String jsonContent = objectMapper.writeValueAsString(sedeDTO);
        mockMvc.perform(post("/api/sedes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Estadio Olímpico"));
    }

    @Test
    @SuppressWarnings("null")
    void actualizar_WithValidData_ShouldReturnUpdatedSede() throws Exception {
        when(sedeService.actualizar(eq(1L), any(SedeDTO.class))).thenReturn(sedeResponse);

        String jsonContent = objectMapper.writeValueAsString(sedeDTO);
        mockMvc.perform(put("/api/sedes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminar_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/sedes/1"))
                .andExpect(status().isNoContent());
    }
}


