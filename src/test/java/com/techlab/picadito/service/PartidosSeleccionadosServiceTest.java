package com.techlab.picadito.service;

import com.techlab.picadito.dto.PartidosSeleccionadosDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.LineaPartidoSeleccionado;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Participante;
import com.techlab.picadito.model.PartidosSeleccionados;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.repository.PartidosSeleccionadosRepository;
import com.techlab.picadito.util.MapperUtil;
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
class PartidosSeleccionadosServiceTest {

    @Mock
    private PartidosSeleccionadosRepository partidosSeleccionadosRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PartidoService partidoService;

    @Mock
    private MapperUtil mapperUtil;

    @InjectMocks
    private PartidosSeleccionadosService partidosSeleccionadosService;

    private PartidosSeleccionados partidosSeleccionados;
    private PartidosSeleccionadosDTO partidosSeleccionadosDTO;
    private Partido partido;
    private Usuario usuario;

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
        // cantidadParticipantes es calculado, así que agregamos participantes
        for (int i = 0; i < 5; i++) {
            Participante p = new Participante();
            p.setId((long) i);
            p.setNombre("Participante " + i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        }

        partidosSeleccionados = new PartidosSeleccionados();
        partidosSeleccionados.setId(1L);
        partidosSeleccionados.setUsuario(usuario);
        partidosSeleccionados.setItems(new ArrayList<>());

        partidosSeleccionadosDTO = new PartidosSeleccionadosDTO();
        partidosSeleccionadosDTO.setId(1L);
        partidosSeleccionadosDTO.setUsuarioId(1L);
        partidosSeleccionadosDTO.setItems(new ArrayList<>());
    }

    @Test
    void obtenerPartidosSeleccionadosPorUsuario_WhenExists_ShouldReturnPartidos() {
        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(mapperUtil.toPartidosSeleccionadosDTO(partidosSeleccionados))
                .thenReturn(partidosSeleccionadosDTO);

        PartidosSeleccionadosDTO result = partidosSeleccionadosService.obtenerPartidosSeleccionadosPorUsuario(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(partidosSeleccionadosRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void obtenerPartidosSeleccionadosPorUsuario_WhenNotExists_ShouldCreateNew() {
        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(usuario);
        when(partidosSeleccionadosRepository.save(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionados);
        when(mapperUtil.toPartidosSeleccionadosDTO(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionadosDTO);

        PartidosSeleccionadosDTO result = partidosSeleccionadosService.obtenerPartidosSeleccionadosPorUsuario(1L);

        assertNotNull(result);
        verify(partidosSeleccionadosRepository, times(1)).save(any(PartidosSeleccionados.class));
    }

    @Test
    void agregarPartido_WithValidData_ShouldAddPartido() {
        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);
        when(partidosSeleccionadosRepository.save(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionados);
        when(mapperUtil.toPartidosSeleccionadosDTO(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionadosDTO);

        PartidosSeleccionadosDTO result = partidosSeleccionadosService.agregarPartido(1L, 1L, 2);

        assertNotNull(result);
        verify(partidosSeleccionadosRepository, times(1)).save(any(PartidosSeleccionados.class));
    }

    @Test
    void agregarPartido_WhenPartidoNotAvailable_ShouldThrowException() {
        partido.setEstado(EstadoPartido.COMPLETO);
        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);

        assertThrows(BusinessException.class, () -> {
            partidosSeleccionadosService.agregarPartido(1L, 1L, 2);
        });
    }

    @Test
    void agregarPartido_WhenPartidoCompleto_ShouldThrowException() {
        // cantidadParticipantes es calculado, así que agregamos participantes hasta completar
        for (int i = 0; i < 10; i++) {
            Participante p = new Participante();
            p.setId((long) i);
            p.setNombre("Participante " + i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        }
        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);

        assertThrows(BusinessException.class, () -> {
            partidosSeleccionadosService.agregarPartido(1L, 1L, 2);
        });
    }

    @Test
    void agregarPartido_WhenExceedsCapacity_ShouldThrowException() {
        // cantidadParticipantes es calculado, así que agregamos participantes
        for (int i = 0; i < 9; i++) {
            Participante p = new Participante();
            p.setId((long) i);
            p.setNombre("Participante " + i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        }
        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);

        assertThrows(BusinessException.class, () -> {
            partidosSeleccionadosService.agregarPartido(1L, 1L, 2);
        });
    }

    @Test
    void actualizarCantidad_WithValidData_ShouldUpdateQuantity() {
        LineaPartidoSeleccionado linea = new LineaPartidoSeleccionado();
        linea.setId(1L);
        linea.setPartido(partido);
        linea.setCantidad(2);
        partidosSeleccionados.getItems().add(linea);

        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(partidosSeleccionadosRepository.save(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionados);
        when(mapperUtil.toPartidosSeleccionadosDTO(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionadosDTO);

        PartidosSeleccionadosDTO result = partidosSeleccionadosService.actualizarCantidad(1L, 1L, 3);

        assertNotNull(result);
        verify(partidosSeleccionadosRepository, times(1)).save(any(PartidosSeleccionados.class));
    }

    @Test
    void actualizarCantidad_WithZeroQuantity_ShouldRemoveItem() {
        LineaPartidoSeleccionado linea = new LineaPartidoSeleccionado();
        linea.setId(1L);
        linea.setPartido(partido);
        linea.setCantidad(2);
        partidosSeleccionados.getItems().add(linea);

        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(partidosSeleccionadosRepository.save(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionados);
        when(mapperUtil.toPartidosSeleccionadosDTO(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionadosDTO);

        PartidosSeleccionadosDTO result = partidosSeleccionadosService.actualizarCantidad(1L, 1L, 0);

        assertNotNull(result);
        assertTrue(partidosSeleccionados.getItems().isEmpty());
    }

    @Test
    void eliminarItem_WithValidId_ShouldRemoveItem() {
        LineaPartidoSeleccionado linea = new LineaPartidoSeleccionado();
        linea.setId(1L);
        linea.setPartido(partido);
        partidosSeleccionados.getItems().add(linea);

        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(partidosSeleccionadosRepository.save(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionados);

        partidosSeleccionadosService.eliminarItem(1L, 1L);

        assertTrue(partidosSeleccionados.getItems().isEmpty());
        verify(partidosSeleccionadosRepository, times(1)).save(any(PartidosSeleccionados.class));
    }

    @Test
    void eliminarItem_WhenNotExists_ShouldThrowException() {
        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            partidosSeleccionadosService.eliminarItem(1L, 1L);
        });
    }

    @Test
    void vaciarPartidosSeleccionados_WithValidId_ShouldClearAll() {
        LineaPartidoSeleccionado linea = new LineaPartidoSeleccionado();
        linea.setId(1L);
        partidosSeleccionados.getItems().add(linea);

        when(partidosSeleccionadosRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(partidosSeleccionados));
        when(partidosSeleccionadosRepository.save(any(PartidosSeleccionados.class)))
                .thenReturn(partidosSeleccionados);

        partidosSeleccionadosService.vaciarPartidosSeleccionados(1L);

        assertTrue(partidosSeleccionados.getItems().isEmpty());
        verify(partidosSeleccionadosRepository, times(1)).save(any(PartidosSeleccionados.class));
    }
}

