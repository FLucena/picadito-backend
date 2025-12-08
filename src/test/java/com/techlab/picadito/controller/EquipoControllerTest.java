package com.techlab.picadito.controller;

import com.techlab.picadito.dto.EquipoResponseDTO;
import com.techlab.picadito.dto.EquiposResponseDTO;
import com.techlab.picadito.equipo.EquipoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.techlab.picadito.equipo.EquipoController.class)
class EquipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private EquipoService equipoService;

    private EquipoResponseDTO equipoResponse;

    @BeforeEach
    void setUp() {
        equipoResponse = new EquipoResponseDTO();
        equipoResponse.setId(1L);
        equipoResponse.setNombre("Equipo A");
        equipoResponse.setPartidoId(1L);
        equipoResponse.setCantidadParticipantes(11);
    }

    @Test
    void generarEquiposAutomaticos_ShouldReturnListOfEquipos() throws Exception {
        EquipoResponseDTO equipo2 = new EquipoResponseDTO();
        equipo2.setId(2L);
        equipo2.setNombre("Equipo B");
        equipo2.setPartidoId(1L);
        equipo2.setCantidadParticipantes(11);

        List<EquipoResponseDTO> equipos = Arrays.asList(equipoResponse, equipo2);
        EquiposResponseDTO equiposResponse = new EquiposResponseDTO(equipos);
        when(equipoService.generarEquiposAutomaticos(1L)).thenReturn(equiposResponse);

        mockMvc.perform(post("/api/equipos/partido/1/generar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.equipos[0].id").value(1))
                .andExpect(jsonPath("$.equipos[0].nombre").value("Equipo A"))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void obtenerEquiposPorPartido_ShouldReturnListOfEquipos() throws Exception {
        List<EquipoResponseDTO> equipos = Arrays.asList(equipoResponse);
        EquiposResponseDTO equiposResponse = new EquiposResponseDTO(equipos);
        when(equipoService.obtenerEquiposPorPartido(1L)).thenReturn(equiposResponse);

        mockMvc.perform(get("/api/equipos/partido/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.equipos[0].id").value(1))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnEquipo() throws Exception {
        when(equipoService.obtenerPorId(1L)).thenReturn(equipoResponse);

        mockMvc.perform(get("/api/equipos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Equipo A"));
    }

    @Test
    void eliminarEquipos_WithValidPartidoId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/equipos/partido/1"))
                .andExpect(status().isNoContent());
    }
}


