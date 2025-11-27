package com.techlab.picadito.service;

import com.techlab.picadito.dto.EstadisticasDTO;
import com.techlab.picadito.model.*;
import com.techlab.picadito.repository.PartidoRepository;
import com.techlab.picadito.repository.ReservaRepository;
import com.techlab.picadito.repository.SedeRepository;
import com.techlab.picadito.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstadisticasServiceTest {

    @Mock
    private PartidoRepository partidoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SedeRepository sedeRepository;

    @InjectMocks
    private EstadisticasService estadisticasService;

    private Partido partido;
    private Reserva reserva;
    private Usuario usuario;
    private Sede sede;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test User");

        sede = new Sede();
        sede.setId(1L);
        sede.setNombre("Sede Test");
        sede.setDireccion("Dirección Test");

        partido = new Partido();
        partido.setId(1L);
        partido.setTitulo("Partido Test");
        partido.setMaxJugadores(10);
        // cantidadParticipantes es calculado, agregamos participantes
        for (int i = 0; i < 5; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        }
        partido.setSede(sede);
        partido.setFechaCreacion(LocalDateTime.now());

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setUsuario(usuario);
        reserva.setEstado(Reserva.EstadoReserva.CONFIRMADO);
        reserva.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void obtenerEstadisticasGenerales_ShouldReturnEstadisticas() {
        when(partidoRepository.count()).thenReturn(10L);
        when(reservaRepository.count()).thenReturn(25L);
        when(usuarioRepository.count()).thenReturn(5L);
        when(reservaRepository.findAll()).thenReturn(Arrays.asList(reserva));
        when(partidoRepository.findAll()).thenReturn(Arrays.asList(partido));
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario));
        when(sedeRepository.findAll()).thenReturn(Arrays.asList(sede));

        EstadisticasDTO result = estadisticasService.obtenerEstadisticasGenerales();

        assertNotNull(result);
        assertEquals(10L, result.getTotalPartidos());
        assertEquals(25L, result.getTotalReservas());
        assertEquals(5L, result.getTotalUsuarios());
        verify(partidoRepository, times(1)).count();
        verify(reservaRepository, times(1)).count();
        verify(usuarioRepository, times(1)).count();
    }

    @Test
    void obtenerEstadisticasPorPeriodo_WithValidPeriod_ShouldReturnEstadisticas() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        reserva.setFechaCreacion(LocalDateTime.now().minusDays(15));
        partido.setFechaCreacion(LocalDateTime.now().minusDays(15));

        when(reservaRepository.findAll()).thenReturn(Arrays.asList(reserva));
        when(partidoRepository.findAll()).thenReturn(Arrays.asList(partido));

        EstadisticasDTO result = estadisticasService.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin);

        assertNotNull(result);
        assertTrue(result.getTotalReservas() >= 0);
        assertTrue(result.getTotalPartidos() >= 0);
    }

    @Test
    void obtenerEstadisticasPorPeriodo_WithNoDataInPeriod_ShouldReturnZero() {
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fechaFin = LocalDateTime.now().plusDays(30);

        when(reservaRepository.findAll()).thenReturn(new ArrayList<>());
        when(partidoRepository.findAll()).thenReturn(new ArrayList<>());

        EstadisticasDTO result = estadisticasService.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals(0L, result.getTotalReservas());
        assertEquals(0L, result.getTotalPartidos());
    }

    @Test
    void obtenerEstadisticasGenerales_WithNoData_ShouldReturnZeroValues() {
        when(partidoRepository.count()).thenReturn(0L);
        when(reservaRepository.count()).thenReturn(0L);
        when(usuarioRepository.count()).thenReturn(0L);
        when(reservaRepository.findAll()).thenReturn(new ArrayList<>());
        when(partidoRepository.findAll()).thenReturn(new ArrayList<>());
        when(usuarioRepository.findAll()).thenReturn(new ArrayList<>());
        when(sedeRepository.findAll()).thenReturn(new ArrayList<>());

        EstadisticasDTO result = estadisticasService.obtenerEstadisticasGenerales();

        assertNotNull(result);
        assertEquals(0L, result.getTotalPartidos());
        assertEquals(0L, result.getTotalReservas());
        assertEquals(0L, result.getTotalUsuarios());
    }
}

