package com.techlab.picadito.controller;

import com.techlab.picadito.dto.LineaPartidoSeleccionadoDTO;
import com.techlab.picadito.dto.PartidosSeleccionadosDTO;
import com.techlab.picadito.partidosseleccionados.PartidosSeleccionadosService;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.techlab.picadito.partidosseleccionados.PartidosSeleccionadosController.class)
class PartidosSeleccionadosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private PartidosSeleccionadosService partidosSeleccionadosService;

    private PartidosSeleccionadosDTO partidosSeleccionadosDTO;

    @BeforeEach
    void setUp() {
        partidosSeleccionadosDTO = new PartidosSeleccionadosDTO();
        partidosSeleccionadosDTO.setId(1L);
        partidosSeleccionadosDTO.setUsuarioId(1L);
        partidosSeleccionadosDTO.setItems(new ArrayList<>());
        partidosSeleccionadosDTO.setFechaCreacion(LocalDateTime.now());

        LineaPartidoSeleccionadoDTO linea = new LineaPartidoSeleccionadoDTO();
        linea.setId(1L);
        linea.setPartidoId(1L);
        linea.setCantidad(2);
        partidosSeleccionadosDTO.getItems().add(linea);
    }

    @Test
    void obtenerPartidosSeleccionados_WithValidId_ShouldReturnPartidos() throws Exception {
        when(partidosSeleccionadosService.obtenerPartidosSeleccionadosPorUsuario(1L))
                .thenReturn(partidosSeleccionadosDTO);

        mockMvc.perform(get("/api/partidos-seleccionados/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1));
    }

    @Test
    void obtenerPartidosSeleccionados_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/partidos-seleccionados/usuario/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agregarPartido_WithValidData_ShouldReturnPartidosSeleccionados() throws Exception {
        when(partidosSeleccionadosService.agregarPartido(eq(1L), eq(1L), eq(2)))
                .thenReturn(partidosSeleccionadosDTO);

        mockMvc.perform(post("/api/partidos-seleccionados/usuario/1/agregar")
                        .param("partidoId", "1")
                        .param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void agregarPartido_WithInvalidUsuarioId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/partidos-seleccionados/usuario/invalid/agregar")
                        .param("partidoId", "1")
                        .param("cantidad", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarCantidad_WithValidData_ShouldReturnUpdatedPartidos() throws Exception {
        when(partidosSeleccionadosService.actualizarCantidad(eq(1L), eq(1L), eq(3)))
                .thenReturn(partidosSeleccionadosDTO);

        mockMvc.perform(put("/api/partidos-seleccionados/usuario/1/item/1")
                        .param("cantidad", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void actualizarCantidad_WithInvalidIds_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/partidos-seleccionados/usuario/invalid/item/1")
                        .param("cantidad", "3"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/partidos-seleccionados/usuario/1/item/invalid")
                        .param("cantidad", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarItem_WithValidIds_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/partidos-seleccionados/usuario/1/item/1"))
                .andExpect(status().isNoContent());

        verify(partidosSeleccionadosService).eliminarItem(1L, 1L);
    }

    @Test
    void eliminarItem_WithInvalidIds_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/partidos-seleccionados/usuario/invalid/item/1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/partidos-seleccionados/usuario/1/item/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vaciarPartidosSeleccionados_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/partidos-seleccionados/usuario/1"))
                .andExpect(status().isNoContent());

        verify(partidosSeleccionadosService).vaciarPartidosSeleccionados(1L);
    }

    @Test
    void vaciarPartidosSeleccionados_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/partidos-seleccionados/usuario/invalid"))
                .andExpect(status().isBadRequest());
    }
}


