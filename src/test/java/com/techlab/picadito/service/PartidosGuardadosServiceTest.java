package com.techlab.picadito.service;

import com.techlab.picadito.dto.PartidosGuardadosResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.LineaPartidoGuardado;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.PartidosGuardados;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.repository.PartidosGuardadosRepository;
import com.techlab.picadito.repository.PartidoRepository;
import com.techlab.picadito.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PartidosGuardadosServiceTest {

    @Mock
    private PartidosGuardadosRepository partidosGuardadosRepository;

    @Mock
    private PartidoRepository partidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PartidosGuardadosService partidosGuardadosService;

    private Usuario usuario;
    private Partido partido;
    private PartidosGuardados partidosGuardados;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test User");

        partido = new Partido();
        partido.setId(1L);
        partido.setTitulo("Partido Test");
        partido.setEstado(EstadoPartido.DISPONIBLE);
        partido.setMaxJugadores(10);
        partido.setFechaHora(LocalDateTime.now().plusDays(1));
        // cantidadParticipantes es calculado, agregamos participantes
        for (int i = 0; i < 5; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        }

        partidosGuardados = new PartidosGuardados();
        partidosGuardados.setId(1L);
        partidosGuardados.setUsuario(usuario);
        partidosGuardados.setPartidos(new ArrayList<>());
    }

    @Test
    void obtenerPartidosGuardadosPorUsuario_WithValidId_ShouldReturnPartidosGuardados() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(partidosGuardadosRepository.findByUsuario(usuario)).thenReturn(Optional.of(partidosGuardados));

        PartidosGuardadosResponseDTO result = partidosGuardadosService.obtenerPartidosGuardadosPorUsuario(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUsuarioId());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(partidosGuardadosRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    void obtenerPartidosGuardadosPorUsuario_WhenNotExists_ShouldCreateNew() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(partidosGuardadosRepository.findByUsuario(usuario)).thenReturn(Optional.empty());
        when(partidosGuardadosRepository.save(any(PartidosGuardados.class))).thenReturn(partidosGuardados);

        PartidosGuardadosResponseDTO result = partidosGuardadosService.obtenerPartidosGuardadosPorUsuario(1L);

        assertNotNull(result);
        verify(partidosGuardadosRepository, times(1)).save(any(PartidosGuardados.class));
    }

    @Test
    void obtenerPartidosGuardadosPorUsuario_WithInvalidId_ShouldThrowException() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            partidosGuardadosService.obtenerPartidosGuardadosPorUsuario(999L);
        });
    }

    @Test
    void agregarPartido_WithValidData_ShouldAddPartido() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        when(partidosGuardadosRepository.findByUsuario(usuario)).thenReturn(Optional.of(partidosGuardados));
        when(partidosGuardadosRepository.save(any(PartidosGuardados.class))).thenReturn(partidosGuardados);

        PartidosGuardadosResponseDTO result = partidosGuardadosService.agregarPartido(1L, 1L);

        assertNotNull(result);
        verify(partidosGuardadosRepository, times(1)).save(any(PartidosGuardados.class));
    }

    @Test
    void agregarPartido_WithPartidoNotDisponible_ShouldThrowException() {
        partido.setEstado(EstadoPartido.COMPLETO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));

        assertThrows(BusinessException.class, () -> {
            partidosGuardadosService.agregarPartido(1L, 1L);
        });
    }

    @Test
    void agregarPartido_WithPartidoCompleto_ShouldThrowException() {
        // Llenar el partido hasta el máximo
        partido.getParticipantes().clear();
        for (int i = 0; i < 10; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        }
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));

        assertThrows(BusinessException.class, () -> {
            partidosGuardadosService.agregarPartido(1L, 1L);
        });
    }

    @Test
    void agregarPartido_WithDuplicatePartido_ShouldThrowException() {
        LineaPartidoGuardado linea = new LineaPartidoGuardado();
        linea.setPartido(partido);
        partidosGuardados.getPartidos().add(linea);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        when(partidosGuardadosRepository.findByUsuario(usuario)).thenReturn(Optional.of(partidosGuardados));

        assertThrows(BusinessException.class, () -> {
            partidosGuardadosService.agregarPartido(1L, 1L);
        });
    }

    @Test
    void eliminarPartido_WithValidIds_ShouldRemovePartido() {
        LineaPartidoGuardado linea = new LineaPartidoGuardado();
        linea.setId(1L);
        linea.setPartido(partido);
        partidosGuardados.getPartidos().add(linea);

        when(partidosGuardadosRepository.findByUsuarioId(1L)).thenReturn(Optional.of(partidosGuardados));
        when(partidosGuardadosRepository.save(any(PartidosGuardados.class))).thenReturn(partidosGuardados);

        PartidosGuardadosResponseDTO result = partidosGuardadosService.eliminarPartido(1L, 1L);

        assertNotNull(result);
        verify(partidosGuardadosRepository, times(1)).save(any(PartidosGuardados.class));
    }

    @Test
    void eliminarPartido_WithInvalidUsuarioId_ShouldThrowException() {
        when(partidosGuardadosRepository.findByUsuarioId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            partidosGuardadosService.eliminarPartido(999L, 1L);
        });
    }

    @Test
    void eliminarPartido_WithInvalidLineaId_ShouldThrowException() {
        when(partidosGuardadosRepository.findByUsuarioId(1L)).thenReturn(Optional.of(partidosGuardados));

        assertThrows(ResourceNotFoundException.class, () -> {
            partidosGuardadosService.eliminarPartido(1L, 999L);
        });
    }

    @Test
    void vaciarPartidosGuardados_WithValidId_ShouldClearPartidos() {
        when(partidosGuardadosRepository.findByUsuarioId(1L)).thenReturn(Optional.of(partidosGuardados));
        when(partidosGuardadosRepository.save(any(PartidosGuardados.class))).thenReturn(partidosGuardados);

        partidosGuardadosService.vaciarPartidosGuardados(1L);

        verify(partidosGuardadosRepository, times(1)).save(any(PartidosGuardados.class));
    }

    @Test
    void vaciarPartidosGuardados_WithInvalidId_ShouldThrowException() {
        when(partidosGuardadosRepository.findByUsuarioId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            partidosGuardadosService.vaciarPartidosGuardados(999L);
        });
    }
}

