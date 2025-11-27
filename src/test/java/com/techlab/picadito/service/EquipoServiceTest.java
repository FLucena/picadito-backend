package com.techlab.picadito.service;

import com.techlab.picadito.dto.EquipoResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Equipo;
import com.techlab.picadito.model.Nivel;
import com.techlab.picadito.model.Participante;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Posicion;
import com.techlab.picadito.repository.EquipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class EquipoServiceTest {

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private PartidoService partidoService;

    @InjectMocks
    private EquipoService equipoService;

    private Partido partido;
    private List<Participante> participantes;

    @BeforeEach
    void setUp() {
        partido = new Partido();
        partido.setId(1L);
        partido.setTitulo("Partido Test");
        partido.setParticipantes(new ArrayList<>());

        participantes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Participante p = new Participante();
            p.setId((long) i);
            p.setNombre("Participante " + i);
            p.setPosicion(i % 2 == 0 ? Posicion.DELANTERO : Posicion.DEFENSA);
            p.setNivel(i < 3 ? Nivel.EXPERTO : Nivel.INTERMEDIO);
            p.setPartido(partido);
            participantes.add(p);
            partido.getParticipantes().add(p);
        }
    }

    @Test
    void generarEquiposAutomaticos_WithValidData_ShouldGenerateTeams() {
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);
        when(equipoRepository.findByPartidoId(1L)).thenReturn(new ArrayList<>());
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(invocation -> {
            Equipo equipo = invocation.getArgument(0);
            equipo.setId(1L);
            return equipo;
        });

        List<EquipoResponseDTO> result = equipoService.generarEquiposAutomaticos(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(equipoRepository, times(2)).save(any(Equipo.class));
    }

    @Test
    void generarEquiposAutomaticos_WithInsufficientParticipants_ShouldThrowException() {
        partido.getParticipantes().clear();
        Participante p = new Participante();
        p.setId(1L);
        partido.getParticipantes().add(p);

        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);

        assertThrows(BusinessException.class, () -> {
            equipoService.generarEquiposAutomaticos(1L);
        });
    }

    @Test
    void generarEquiposAutomaticos_WithExistingTeams_ShouldDeleteAndRegenerate() {
        Equipo equipoExistente = new Equipo();
        equipoExistente.setId(1L);
        List<Equipo> equiposExistentes = Arrays.asList(equipoExistente);

        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);
        when(equipoRepository.findByPartidoId(1L)).thenReturn(equiposExistentes);
        doNothing().when(equipoRepository).deleteAll(equiposExistentes);
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(invocation -> {
            Equipo equipo = invocation.getArgument(0);
            equipo.setId(1L);
            return equipo;
        });

        List<EquipoResponseDTO> result = equipoService.generarEquiposAutomaticos(1L);

        assertNotNull(result);
        verify(equipoRepository, times(1)).deleteAll(equiposExistentes);
        verify(equipoRepository, times(2)).save(any(Equipo.class));
    }

    @Test
    void obtenerEquiposPorPartido_WithValidId_ShouldReturnList() {
        Equipo equipo = new Equipo();
        equipo.setId(1L);
        equipo.setNombre("Equipo A");
        equipo.setPartido(partido);
        List<Equipo> equipos = Arrays.asList(equipo);

        when(equipoRepository.findByPartidoId(1L)).thenReturn(equipos);

        List<EquipoResponseDTO> result = equipoService.obtenerEquiposPorPartido(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(equipoRepository, times(1)).findByPartidoId(1L);
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnEquipo() {
        Equipo equipo = new Equipo();
        equipo.setId(1L);
        equipo.setNombre("Equipo A");
        equipo.setPartido(partido);

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));

        EquipoResponseDTO result = equipoService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Equipo A", result.getNombre());
    }

    @Test
    void obtenerPorId_WithInvalidId_ShouldThrowException() {
        when(equipoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            equipoService.obtenerPorId(999L);
        });
    }

    @Test
    void eliminarEquipos_WithValidId_ShouldDeleteEquipos() {
        Equipo equipo = new Equipo();
        equipo.setId(1L);
        List<Equipo> equipos = Arrays.asList(equipo);

        when(equipoRepository.findByPartidoId(1L)).thenReturn(equipos);
        doNothing().when(equipoRepository).deleteAll(equipos);

        equipoService.eliminarEquipos(1L);

        verify(equipoRepository, times(1)).deleteAll(equipos);
    }

    @Test
    void eliminarEquipos_WithNoEquipos_ShouldNotThrowException() {
        when(equipoRepository.findByPartidoId(1L)).thenReturn(new ArrayList<>());

        equipoService.eliminarEquipos(1L);

        verify(equipoRepository, never()).deleteAll(any());
    }
}

