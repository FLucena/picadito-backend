package com.techlab.picadito.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.picadito.dto.AlertaDTO;
import com.techlab.picadito.dto.AlertaResponseDTO;
import com.techlab.picadito.dto.AlertasResponseDTO;
import com.techlab.picadito.model.TipoAlerta;
import com.techlab.picadito.alerta.AlertaService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.techlab.picadito.alerta.AlertaController.class)
class AlertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private AlertaService alertaService;

    @Autowired
    private ObjectMapper objectMapper;

    private AlertaResponseDTO alertaResponse;
    private AlertaDTO alertaDTO;

    @BeforeEach
    void setUp() {
        alertaResponse = new AlertaResponseDTO();
        alertaResponse.setId(1L);
        alertaResponse.setTipo(TipoAlerta.CUPOS_BAJOS);
        alertaResponse.setMensaje("El partido tiene solo 3 cupos disponibles");
        alertaResponse.setLeida(false);
        alertaResponse.setUsuarioId(1L);
        alertaResponse.setPartidoId(1L);
        alertaResponse.setFechaCreacion(LocalDateTime.now());

        alertaDTO = new AlertaDTO();
        alertaDTO.setTipo(TipoAlerta.CUPOS_BAJOS);
        alertaDTO.setMensaje("El partido tiene solo 3 cupos disponibles");
        alertaDTO.setUsuarioId(1L);
        alertaDTO.setPartidoId(1L);
    }

    @Test
    void obtenerPorUsuario_ShouldReturnListOfAlertas() throws Exception {
        List<AlertaResponseDTO> alertas = Arrays.asList(alertaResponse);
        AlertasResponseDTO alertasResponse = new AlertasResponseDTO(alertas);
        when(alertaService.obtenerPorUsuario(1L)).thenReturn(alertasResponse);

        mockMvc.perform(get("/api/alertas/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.alertas[0].id").value(1))
                .andExpect(jsonPath("$.alertas[0].tipo").value("CUPOS_BAJOS"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void obtenerNoLeidasPorUsuario_ShouldReturnListOfUnreadAlertas() throws Exception {
        List<AlertaResponseDTO> alertas = Arrays.asList(alertaResponse);
        AlertasResponseDTO alertasResponse = new AlertasResponseDTO(alertas);
        when(alertaService.obtenerNoLeidasPorUsuario(1L)).thenReturn(alertasResponse);

        mockMvc.perform(get("/api/alertas/usuario/1/no-leidas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.alertas[0].leida").value(false))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @SuppressWarnings("null")
    void crear_WithValidData_ShouldReturnCreated() throws Exception {
        when(alertaService.crear(any(AlertaDTO.class))).thenReturn(alertaResponse);

        String jsonContent = objectMapper.writeValueAsString(alertaDTO);
        mockMvc.perform(post("/api/alertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void marcarComoLeida_ShouldReturnUpdatedAlerta() throws Exception {
        alertaResponse.setLeida(true);
        when(alertaService.marcarComoLeida(1L)).thenReturn(alertaResponse);

        mockMvc.perform(put("/api/alertas/1/marcar-leida"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    void eliminar_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/alertas/1"))
                .andExpect(status().isNoContent());
    }
}


