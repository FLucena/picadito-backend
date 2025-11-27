package com.techlab.picadito.service;

import com.techlab.picadito.dto.CalificacionDTO;
import com.techlab.picadito.dto.CalificacionResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Calificacion;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.repository.CalificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CalificacionServiceTest {

    @Mock
    private CalificacionRepository calificacionRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PartidoService partidoService;

    @InjectMocks
    private CalificacionService calificacionService;

    private Calificacion calificacion;
    private CalificacionDTO calificacionDTO;
    private Usuario usuario;
    private Partido partido;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test User");

        partido = new Partido();
        partido.setId(1L);
        partido.setTitulo("Partido Test");
        partido.setEstado(EstadoPartido.FINALIZADO);

        calificacion = new Calificacion();
        calificacion.setId(1L);
        calificacion.setPuntuacion(5);
        calificacion.setComentario("Excelente partido");
        calificacion.setUsuario(usuario);
        calificacion.setPartido(partido);

        calificacionDTO = new CalificacionDTO();
        calificacionDTO.setPartidoId(1L);
        calificacionDTO.setPuntuacion(5);
        calificacionDTO.setComentario("Excelente partido");
    }

    @Test
    void crear_WithValidData_ShouldCreateCalificacion() {
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(usuario);
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);
        when(calificacionRepository.existsByUsuarioIdAndPartidoId(1L, 1L)).thenReturn(false);
        when(calificacionRepository.save(any(Calificacion.class))).thenReturn(calificacion);

        CalificacionResponseDTO result = calificacionService.crear(1L, calificacionDTO);

        assertNotNull(result);
        assertEquals(5, result.getPuntuacion());
        verify(calificacionRepository, times(1)).save(any(Calificacion.class));
    }

    @Test
    void crear_WithPartidoNotFinalizado_ShouldThrowException() {
        partido.setEstado(EstadoPartido.DISPONIBLE);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(usuario);
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);

        assertThrows(BusinessException.class, () -> {
            calificacionService.crear(1L, calificacionDTO);
        });
    }

    @Test
    void crear_WithDuplicateCalificacion_ShouldThrowException() {
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(usuario);
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);
        when(calificacionRepository.existsByUsuarioIdAndPartidoId(1L, 1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            calificacionService.crear(1L, calificacionDTO);
        });
    }

    @Test
    void obtenerPorPartido_WithValidId_ShouldReturnList() {
        List<Calificacion> calificaciones = Arrays.asList(calificacion);
        when(calificacionRepository.findByPartidoIdOrderByFechaCreacionDesc(1L)).thenReturn(calificaciones);

        List<CalificacionResponseDTO> result = calificacionService.obtenerPorPartido(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(calificacionRepository, times(1)).findByPartidoIdOrderByFechaCreacionDesc(1L);
    }

    @Test
    void obtenerPromedioPorPartido_WithValidId_ShouldReturnAverage() {
        when(calificacionRepository.calcularPromedioPorPartido(1L)).thenReturn(4.5);

        Double result = calificacionService.obtenerPromedioPorPartido(1L);

        assertNotNull(result);
        assertEquals(4.5, result);
        verify(calificacionRepository, times(1)).calcularPromedioPorPartido(1L);
    }

    @Test
    void obtenerPromedioPorPartido_WithNoCalificaciones_ShouldReturnZero() {
        when(calificacionRepository.calcularPromedioPorPartido(1L)).thenReturn(null);

        Double result = calificacionService.obtenerPromedioPorPartido(1L);

        assertNotNull(result);
        assertEquals(0.0, result);
    }

    @Test
    void obtenerPromedioPorCreador_WithValidName_ShouldReturnAverage() {
        when(calificacionRepository.calcularPromedioPorCreador("Test Creator")).thenReturn(4.0);

        Double result = calificacionService.obtenerPromedioPorCreador("Test Creator");

        assertNotNull(result);
        assertEquals(4.0, result);
    }

    @Test
    void obtenerPromedioPorSede_WithValidId_ShouldReturnAverage() {
        when(calificacionRepository.calcularPromedioPorSede(1L)).thenReturn(4.2);

        Double result = calificacionService.obtenerPromedioPorSede(1L);

        assertNotNull(result);
        assertEquals(4.2, result);
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnCalificacion() {
        when(calificacionRepository.findById(1L)).thenReturn(Optional.of(calificacion));

        CalificacionResponseDTO result = calificacionService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(5, result.getPuntuacion());
    }

    @Test
    void obtenerPorId_WithInvalidId_ShouldThrowException() {
        when(calificacionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            calificacionService.obtenerPorId(999L);
        });
    }

    @Test
    void eliminar_WithValidId_ShouldDeleteCalificacion() {
        when(calificacionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(calificacionRepository).deleteById(1L);

        calificacionService.eliminar(1L);

        verify(calificacionRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_WithInvalidId_ShouldThrowException() {
        when(calificacionRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            calificacionService.eliminar(999L);
        });
    }
}

