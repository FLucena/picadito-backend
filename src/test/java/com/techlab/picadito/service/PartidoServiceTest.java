package com.techlab.picadito.service;

import com.techlab.picadito.dto.PageResponseDTO;
import com.techlab.picadito.dto.PartidoDTO;
import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.exception.ValidationException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.partido.PartidoRepository;
import com.techlab.picadito.sede.SedeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Mock
    private com.techlab.picadito.categoria.CategoriaService categoriaService;

    @Mock
    private com.techlab.picadito.alerta.AlertaService alertaService;

    @Mock
    private com.techlab.picadito.calificacion.CalificacionService calificacionService;

    @Mock
    private com.techlab.picadito.equipo.EquipoService equipoService;

    @InjectMocks
    private com.techlab.picadito.partido.PartidoService partidoService;

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
    @SuppressWarnings("null")
    void obtenerTodosLosPartidos_ShouldReturnListOfPartidos() {
        List<Partido> partidos = Arrays.asList(partido);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Partido> partidosPage = new PageImpl<>(partidos, pageable, 1);
        // Mock del método findAll de JpaRepository (sin Specification)
        when(partidoRepository.findAll(pageable)).thenReturn(partidosPage);

        PageResponseDTO<PartidoResponseDTO> result = partidoService.obtenerTodosLosPartidos(pageable);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
        assertEquals("Partido de Prueba", result.getContent().get(0).getTitulo());
        assertEquals(1, result.getTotalElements());
        verify(partidoRepository, times(1)).findAll(pageable);
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
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(savedPartido));
        doNothing().when(alertaService).crearAlertaCuposBajos(any(Partido.class));

        PartidoResponseDTO result = partidoService.crearPartido(partidoDTO);

        assertNotNull(result);
        assertEquals("Partido de Prueba", result.getTitulo());
        verify(partidoRepository, times(1)).save(any(Partido.class));
        verify(partidoRepository, times(1)).findById(1L);
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

    @Test
    void eliminarPartido_WithInvalidId_ShouldThrowException() {
        when(partidoRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            partidoService.eliminarPartido(999L);
        });

        verify(partidoRepository, times(1)).existsById(999L);
        verify(partidoRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarPartido_WithDataIntegrityViolation_ShouldThrowBusinessException() {
        when(partidoRepository.existsById(1L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Foreign key constraint violation"))
                .when(partidoRepository).deleteById(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            partidoService.eliminarPartido(1L);
        });

        assertTrue(exception.getMessage().contains("No se puede eliminar el partido"));
        assertTrue(exception.getMessage().contains("participantes") || 
                   exception.getMessage().contains("reservas") ||
                   exception.getMessage().contains("equipos"));
        
        verify(partidoRepository, times(1)).existsById(1L);
        verify(partidoRepository, times(1)).deleteById(1L);
    }
}


