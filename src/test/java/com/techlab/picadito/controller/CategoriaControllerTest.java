package com.techlab.picadito.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techlab.picadito.dto.CategoriaDTO;
import com.techlab.picadito.dto.CategoriaResponseDTO;
import com.techlab.picadito.dto.CategoriasResponseDTO;
import com.techlab.picadito.categoria.CategoriaService;
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

@WebMvcTest(com.techlab.picadito.categoria.CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private CategoriaService categoriaService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoriaResponseDTO categoriaResponse;
    private CategoriaDTO categoriaDTO;

    @BeforeEach
    void setUp() {
        categoriaResponse = new CategoriaResponseDTO();
        categoriaResponse.setId(1L);
        categoriaResponse.setNombre("Fútbol 11");
        categoriaResponse.setDescripcion("Partidos de fútbol 11 vs 11");
        categoriaResponse.setIcono("⚽");
        categoriaResponse.setColor("#1E88E5");
        categoriaResponse.setFechaCreacion(LocalDateTime.now());

        categoriaDTO = new CategoriaDTO();
        categoriaDTO.setNombre("Fútbol 11");
        categoriaDTO.setDescripcion("Partidos de fútbol 11 vs 11");
        categoriaDTO.setIcono("⚽");
        categoriaDTO.setColor("#1E88E5");
    }

    @Test
    void obtenerTodas_ShouldReturnListOfCategorias() throws Exception {
        List<CategoriaResponseDTO> categorias = Arrays.asList(categoriaResponse);
        CategoriasResponseDTO categoriasResponse = new CategoriasResponseDTO(categorias);
        when(categoriaService.obtenerTodas()).thenReturn(categoriasResponse);

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.categorias[0].id").value(1))
                .andExpect(jsonPath("$.categorias[0].nombre").value("Fútbol 11"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnCategoria() throws Exception {
        when(categoriaService.obtenerPorId(1L)).thenReturn(categoriaResponse);

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Fútbol 11"));
    }

    @Test
    @SuppressWarnings("null")
    void crear_WithValidData_ShouldReturnCreated() throws Exception {
        when(categoriaService.crear(any(CategoriaDTO.class))).thenReturn(categoriaResponse);

        String jsonContent = objectMapper.writeValueAsString(categoriaDTO);
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Fútbol 11"));
    }

    @Test
    @SuppressWarnings("null")
    void actualizar_WithValidData_ShouldReturnUpdatedCategoria() throws Exception {
        when(categoriaService.actualizar(eq(1L), any(CategoriaDTO.class))).thenReturn(categoriaResponse);

        String jsonContent = objectMapper.writeValueAsString(categoriaDTO);
        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminar_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    }
}


