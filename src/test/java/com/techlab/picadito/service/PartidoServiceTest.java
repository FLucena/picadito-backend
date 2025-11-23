package com.techlab.picadito.service;

import com.techlab.picadito.dto.PartidoDTO;
import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.exception.ValidationException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.repository.PartidoRepository;
import com.techlab.picadito.repository.SedeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartidoServiceTest {

    @Mock
    private PartidoRepository partidoRepository;

    @Mock
    private SedeRepository sedeRepository;

    @InjectMocks
    private PartidoService partidoService;

    private Partido partido;
    private PartidoDTO partidoDTO;

    @BeforeEach
    void setUp() {
        partido = new Partido();
        partido.setId(1L);
        partido.setTitulo("Partido de Prueba");
        partido.setDescripcion("Descripción del partido");
        partido.setFechaHora(LocalDateTime.now().plusDays(1));
        partido.setMaxJugadores(10);
        partido.setCreadorNombre("Test User");
        partido.setEstado(EstadoPartido.DISPONIBLE);

        partidoDTO = new PartidoDTO();
        partidoDTO.setTitulo("Partido de Prueba");
        partidoDTO.setDescripcion("Descripción del partido");
        partidoDTO.setFechaHora(LocalDateTime.now().plusDays(1));
        partidoDTO.setMaxJugadores(10);
        partidoDTO.setCreadorNombre("Test User");
    }

    @Test
    void obtenerTodosLosPartidos_ShouldReturnListOfPartidos() {
        List<Partido> partidos = Arrays.asList(partido);
        when(partidoRepository.findAll()).thenReturn(partidos);

        List<PartidoResponseDTO> result = partidoService.obtenerTodosLosPartidos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Partido de Prueba", result.get(0).getTitulo());
        verify(partidoRepository, times(1)).findAll();
    }

    @Test
    void obtenerPartidoPorId_WithValidId_ShouldReturnPartido() {
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));

        PartidoResponseDTO result = partidoService.obtenerPartidoPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Partido de Prueba", result.getTitulo());
        verify(partidoRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPartidoPorId_WithInvalidId_ShouldThrowException() {
        when(partidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            partidoService.obtenerPartidoPorId(999L);
        });
    }

    @Test
    @SuppressWarnings("null")
    void crearPartido_WithValidData_ShouldCreatePartido() {
        Partido savedPartido = partido;
        when(partidoRepository.save(any(Partido.class))).thenReturn(savedPartido);

        PartidoResponseDTO result = partidoService.crearPartido(partidoDTO);

        assertNotNull(result);
        assertEquals("Partido de Prueba", result.getTitulo());
        verify(partidoRepository, times(1)).save(any(Partido.class));
    }

    @Test
    void crearPartido_WithPastDate_ShouldThrowException() {
        partidoDTO.setFechaHora(LocalDateTime.now().minusDays(1));

        assertThrows(ValidationException.class, () -> {
            partidoService.crearPartido(partidoDTO);
        });
    }

    @Test
    @SuppressWarnings("null")
    void actualizarPartido_WithValidData_ShouldUpdatePartido() {
        Partido savedPartido = partido;
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        when(partidoRepository.save(any(Partido.class))).thenReturn(savedPartido);

        PartidoResponseDTO result = partidoService.actualizarPartido(1L, partidoDTO);

        assertNotNull(result);
        verify(partidoRepository, times(1)).findById(1L);
        verify(partidoRepository, times(1)).save(any(Partido.class));
    }

    @Test
    void eliminarPartido_WithValidId_ShouldDeletePartido() {
        when(partidoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(partidoRepository).deleteById(1L);

        partidoService.eliminarPartido(1L);

        verify(partidoRepository, times(1)).existsById(1L);
        verify(partidoRepository, times(1)).deleteById(1L);
    }
}

