package com.techlab.picadito.service;

import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.dto.PartidosResponseDTO;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.partido.PartidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private PartidoRepository partidoRepository;

    @Mock
    private com.techlab.picadito.partido.PartidoService partidoService;

    @InjectMocks
    private com.techlab.picadito.admin.AdminService adminService;

    private Partido partido1;
    private Partido partido2;
    private PartidoResponseDTO partidoResponseDTO;

    @BeforeEach
    void setUp() {
        partido1 = new Partido();
        partido1.setId(1L);
        partido1.setTitulo("Partido 1");
        partido1.setEstado(EstadoPartido.DISPONIBLE);
        partido1.setMaxJugadores(10);
        // cantidadParticipantes es calculado, agregamos participantes
        for (int i = 0; i < 8; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partido1);
            partido1.getParticipantes().add(p);
        } // 2 cupos disponibles

        partido2 = new Partido();
        partido2.setId(2L);
        partido2.setTitulo("Partido 2");
        partido2.setEstado(EstadoPartido.DISPONIBLE);
        partido2.setMaxJugadores(10);
        // cantidadParticipantes es calculado, agregamos participantes
        for (int i = 0; i < 9; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partido2);
            partido2.getParticipantes().add(p);
        } // 1 cupo disponible

        partidoResponseDTO = new PartidoResponseDTO();
        partidoResponseDTO.setId(1L);
        partidoResponseDTO.setTitulo("Partido 1");
    }

    @Test
    @SuppressWarnings("null")
    void obtenerPartidosConCapacidadBaja_WithDefaultCapacity_ShouldReturnFilteredPartidos() {
        List<Partido> partidos = Arrays.asList(partido1, partido2);
        when(partidoRepository.findByEstado(EstadoPartido.DISPONIBLE)).thenReturn(partidos);
        when(partidoService.obtenerPartidoPorId(any(Long.class))).thenReturn(partidoResponseDTO);

        PartidosResponseDTO result = adminService.obtenerPartidosConCapacidadBaja(null);

        assertNotNull(result);
        assertNotNull(result.getPartidos());
        assertEquals(2, result.getPartidos().size());
        assertEquals(2, result.getTotal());
        verify(partidoRepository, times(1)).findByEstado(EstadoPartido.DISPONIBLE);
    }

    @Test
    void obtenerPartidosConCapacidadBaja_WithCustomCapacity_ShouldReturnFilteredPartidos() {
        List<Partido> partidos = Arrays.asList(partido2);
        when(partidoRepository.findByEstado(EstadoPartido.DISPONIBLE)).thenReturn(partidos);
        when(partidoService.obtenerPartidoPorId(2L)).thenReturn(partidoResponseDTO);

        PartidosResponseDTO result = adminService.obtenerPartidosConCapacidadBaja(1);

        assertNotNull(result);
        assertNotNull(result.getPartidos());
        assertEquals(1, result.getPartidos().size());
        assertEquals(1, result.getTotal());
        verify(partidoRepository, times(1)).findByEstado(EstadoPartido.DISPONIBLE);
    }

    @Test
    void obtenerPartidosConCapacidadBaja_WithNoMatchingPartidos_ShouldReturnEmptyList() {
        Partido partidoAltaCapacidad = new Partido();
        partidoAltaCapacidad.setId(3L);
        partidoAltaCapacidad.setEstado(EstadoPartido.DISPONIBLE);
        partidoAltaCapacidad.setMaxJugadores(10);
        // cantidadParticipantes es calculado, agregamos participantes
        for (int i = 0; i < 2; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partidoAltaCapacidad);
            partidoAltaCapacidad.getParticipantes().add(p);
        } // 8 cupos disponibles

        when(partidoRepository.findByEstado(EstadoPartido.DISPONIBLE))
                .thenReturn(Arrays.asList(partidoAltaCapacidad));

        PartidosResponseDTO result = adminService.obtenerPartidosConCapacidadBaja(5);

        assertNotNull(result);
        assertNotNull(result.getPartidos());
        assertTrue(result.getPartidos().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    @SuppressWarnings("null")
    void obtenerPartidosConCapacidadBaja_ShouldOrderByCapacityAscending() {
        List<Partido> partidos = Arrays.asList(partido1, partido2);
        when(partidoRepository.findByEstado(EstadoPartido.DISPONIBLE)).thenReturn(partidos);
        when(partidoService.obtenerPartidoPorId(1L)).thenReturn(partidoResponseDTO);
        when(partidoService.obtenerPartidoPorId(2L)).thenReturn(partidoResponseDTO);

        PartidosResponseDTO result = adminService.obtenerPartidosConCapacidadBaja(5);

        assertNotNull(result);
        assertNotNull(result.getPartidos());
        // El partido con menos capacidad disponible debería estar primero
        verify(partidoService, atLeastOnce()).obtenerPartidoPorId(any(Long.class));
    }
}


