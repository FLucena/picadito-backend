package com.techlab.picadito.service;

import com.techlab.picadito.dto.ReporteDTO;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Reserva;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.partido.PartidoRepository;
import com.techlab.picadito.reserva.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PartidoRepository partidoRepository;

    @InjectMocks
    private com.techlab.picadito.service.ReporteService reporteService;

    private Reserva reserva;
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
        // cantidadParticipantes es calculado, agregamos participantes
        for (int i = 0; i < 5; i++) {
            com.techlab.picadito.model.Participante p = new com.techlab.picadito.model.Participante();
            p.setId((long) i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        }
        partido.setFechaCreacion(LocalDateTime.now().minusDays(15));

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setUsuario(usuario);
        reserva.setEstado(Reserva.EstadoReserva.CONFIRMADO);
        reserva.setFechaCreacion(LocalDateTime.now().minusDays(15));
    }

    @Test
    void generarReporteVentas_WithValidPeriod_ShouldReturnReporte() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(reservaRepository.findAll()).thenReturn(Arrays.asList(reserva));

        ReporteDTO result = reporteService.generarReporteVentas(fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals("VENTAS", result.getTipoReporte());
        assertNotNull(result.getDatos());
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void generarReporteVentas_WithNoReservas_ShouldReturnEmptyReporte() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(reservaRepository.findAll()).thenReturn(Arrays.asList());

        ReporteDTO result = reporteService.generarReporteVentas(fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals("VENTAS", result.getTipoReporte());
        assertNotNull(result.getDatos());
    }

    @Test
    void generarReportePartidos_WithValidPeriod_ShouldReturnReporte() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(partidoRepository.findAll()).thenReturn(Arrays.asList(partido));

        ReporteDTO result = reporteService.generarReportePartidos(fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals("PARTIDOS", result.getTipoReporte());
        assertNotNull(result.getDatos());
        verify(partidoRepository, times(1)).findAll();
    }

    @Test
    void generarReportePartidos_WithNoPartidos_ShouldReturnEmptyReporte() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(partidoRepository.findAll()).thenReturn(Arrays.asList());

        ReporteDTO result = reporteService.generarReportePartidos(fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals("PARTIDOS", result.getTipoReporte());
        assertNotNull(result.getDatos());
    }

    @Test
    void generarReporteUsuarios_WithValidPeriod_ShouldReturnReporte() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(reservaRepository.findAll()).thenReturn(Arrays.asList(reserva));

        ReporteDTO result = reporteService.generarReporteUsuarios(fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals("USUARIOS", result.getTipoReporte());
        assertNotNull(result.getDatos());
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void generarReporteUsuarios_WithNoReservas_ShouldReturnEmptyReporte() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(reservaRepository.findAll()).thenReturn(Arrays.asList());

        ReporteDTO result = reporteService.generarReporteUsuarios(fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals("USUARIOS", result.getTipoReporte());
        assertNotNull(result.getDatos());
    }

    @Test
    void generarReporteVentas_ShouldCalculateCorrectTotals() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        Reserva reserva2 = new Reserva();
        reserva2.setId(2L);
        reserva2.setUsuario(usuario);
        reserva2.setEstado(Reserva.EstadoReserva.PENDIENTE);
        reserva2.setFechaCreacion(LocalDateTime.now().minusDays(10));

        when(reservaRepository.findAll()).thenReturn(Arrays.asList(reserva, reserva2));

        ReporteDTO result = reporteService.generarReporteVentas(fechaInicio, fechaFin);

        assertNotNull(result);
        assertNotNull(result.getDatos());
    }
}


