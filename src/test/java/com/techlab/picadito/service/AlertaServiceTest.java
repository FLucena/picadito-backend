package com.techlab.picadito.service;

import com.techlab.picadito.dto.AlertaDTO;
import com.techlab.picadito.dto.AlertaResponseDTO;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Alerta;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.TipoAlerta;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.repository.AlertaRepository;
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
@SuppressWarnings("null")
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PartidoService partidoService;

    @InjectMocks
    private AlertaService alertaService;

    private Alerta alerta;
    private AlertaDTO alertaDTO;
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

        alerta = new Alerta();
        alerta.setId(1L);
        alerta.setTipo(TipoAlerta.CUPOS_BAJOS);
        alerta.setMensaje("Test message");
        alerta.setLeida(false);
        alerta.setUsuario(usuario);
        alerta.setPartido(partido);

        alertaDTO = new AlertaDTO();
        alertaDTO.setTipo(TipoAlerta.CUPOS_BAJOS);
        alertaDTO.setMensaje("Test message");
        alertaDTO.setUsuarioId(1L);
        alertaDTO.setPartidoId(1L);
    }

    @Test
    void obtenerPorUsuario_WithValidId_ShouldReturnList() {
        List<Alerta> alertas = Arrays.asList(alerta);
        when(alertaRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L)).thenReturn(alertas);

        List<AlertaResponseDTO> result = alertaService.obtenerPorUsuario(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(alertaRepository, times(1)).findByUsuarioIdOrderByFechaCreacionDesc(1L);
    }

    @Test
    void obtenerNoLeidasPorUsuario_WithValidId_ShouldReturnList() {
        List<Alerta> alertas = Arrays.asList(alerta);
        when(alertaRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(1L)).thenReturn(alertas);

        List<AlertaResponseDTO> result = alertaService.obtenerNoLeidasPorUsuario(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(alertaRepository, times(1)).findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(1L);
    }

    @Test
    void crear_WithValidData_ShouldCreateAlerta() {
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(usuario);
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alerta);

        AlertaResponseDTO result = alertaService.crear(alertaDTO);

        assertNotNull(result);
        assertEquals(TipoAlerta.CUPOS_BAJOS, result.getTipo());
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    void crearAlertaCuposBajos_WithLowCapacity_ShouldCreateAlerta() {
        partido.setMaxJugadores(10);
        // cantidadParticipantes es calculado, agregamos participantes
        partido.getParticipantes().clear();
        for (int i = 0; i < 6; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        } // 4 cupos disponibles (<= 5)

        when(alertaRepository.save(any(Alerta.class))).thenReturn(alerta);

        alertaService.crearAlertaCuposBajos(partido);

        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    void crearAlertaCuposBajos_WithHighCapacity_ShouldNotCreateAlerta() {
        partido.setMaxJugadores(10);
        // cantidadParticipantes es calculado, agregamos participantes
        partido.getParticipantes().clear();
        for (int i = 0; i < 3; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        } // 7 cupos disponibles (> 5)

        alertaService.crearAlertaCuposBajos(partido);

        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void crearAlertaPartidoProximo_ShouldCreateAlerta() {
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(usuario);
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alerta);

        alertaService.crearAlertaPartidoProximo(partido, 1L);

        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    void crearAlertaReservaConfirmada_ShouldCreateAlerta() {
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(usuario);
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alerta);

        alertaService.crearAlertaReservaConfirmada(1L, "Partido Test");

        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    void marcarComoLeida_WithValidId_ShouldMarkAsRead() {
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alerta);

        AlertaResponseDTO result = alertaService.marcarComoLeida(1L);

        assertNotNull(result);
        assertTrue(result.getLeida());
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    void marcarComoLeida_WithInvalidId_ShouldThrowException() {
        when(alertaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            alertaService.marcarComoLeida(999L);
        });
    }

    @Test
    void marcarTodasComoLeidas_ShouldMarkAllAsRead() {
        doNothing().when(alertaRepository).marcarTodasComoLeidas(1L);

        alertaService.marcarTodasComoLeidas(1L);

        verify(alertaRepository, times(1)).marcarTodasComoLeidas(1L);
    }

    @Test
    void eliminar_WithValidId_ShouldDeleteAlerta() {
        when(alertaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(alertaRepository).deleteById(1L);

        alertaService.eliminar(1L);

        verify(alertaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_WithInvalidId_ShouldThrowException() {
        when(alertaRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            alertaService.eliminar(999L);
        });
    }

    @Test
    void eliminarAlertasAntiguas_WithOldAlerts_ShouldDeleteAlerts() {
        List<Alerta> alertasAntiguas = Arrays.asList(alerta);
        when(alertaRepository.findAlertasAntiguas(any(LocalDateTime.class))).thenReturn(alertasAntiguas);
        doNothing().when(alertaRepository).deleteAll(alertasAntiguas);

        alertaService.eliminarAlertasAntiguas(30);

        verify(alertaRepository, times(1)).deleteAll(alertasAntiguas);
    }

    @Test
    void eliminarAlertasAntiguas_WithNoOldAlerts_ShouldNotDelete() {
        when(alertaRepository.findAlertasAntiguas(any(LocalDateTime.class))).thenReturn(null);

        alertaService.eliminarAlertasAntiguas(30);

        verify(alertaRepository, never()).deleteAll(any());
    }
}

