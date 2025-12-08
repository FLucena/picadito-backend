package com.techlab.picadito.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.picadito.dto.PartidoDTO;
import com.techlab.picadito.partido.PartidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PartidoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PartidoDTO partidoDTO;

    @BeforeEach
    void setUp() {
        // Limpiar datos antes de cada test
        partidoRepository.deleteAll();
        
        partidoDTO = new PartidoDTO();
        partidoDTO.setTitulo("Partido de Integración");
        partidoDTO.setDescripcion("Test de integración");
        partidoDTO.setFechaHora(LocalDateTime.now().plusDays(1));
        partidoDTO.setMaxJugadores(10);
        partidoDTO.setCreadorNombre("Test User");
    }

    @Test
    @SuppressWarnings("null")
    void createAndRetrievePartido_ShouldWork() throws Exception {
        // Crear partido
        String jsonContent = objectMapper.writeValueAsString(partidoDTO);
        String response = mockMvc.perform(post("/api/partidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Partido de Integración"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer ID del partido creado
        Long partidoId = objectMapper.readTree(response).get("id").asLong();

        // Obtener partido por ID
        mockMvc.perform(get("/api/partidos/" + partidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(partidoId))
                .andExpect(jsonPath("$.titulo").value("Partido de Integración"));
    }

    @Test
    @SuppressWarnings("null")
    void createUpdateAndDeletePartido_ShouldWork() throws Exception {
        // Crear
        String jsonContent = objectMapper.writeValueAsString(partidoDTO);
        String createResponse = mockMvc.perform(post("/api/partidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long partidoId = objectMapper.readTree(createResponse).get("id").asLong();

        // Actualizar
        partidoDTO.setTitulo("Partido Actualizado");
        String updateJsonContent = objectMapper.writeValueAsString(partidoDTO);
        mockMvc.perform(put("/api/partidos/" + partidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Partido Actualizado"));

        // Eliminar
        mockMvc.perform(delete("/api/partidos/" + partidoId))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        mockMvc.perform(get("/api/partidos/" + partidoId))
                .andExpect(status().isNotFound());
    }
}


