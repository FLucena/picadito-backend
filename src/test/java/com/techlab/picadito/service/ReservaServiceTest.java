package com.techlab.picadito.service;

import com.techlab.picadito.dto.LineaPartidoSeleccionadoDTO;
import com.techlab.picadito.dto.PartidosSeleccionadosDTO;
import com.techlab.picadito.dto.ReservaDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.*;
import com.techlab.picadito.model.Participante;
import com.techlab.picadito.repository.ReservaRepository;
import com.techlab.picadito.util.MapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PartidoService partidoService;

    @Mock
    private PartidosSeleccionadosService partidosSeleccionadosService;

    @Mock
    private ParticipanteService participanteService;

    @Mock
    private MapperUtil mapperUtil;

    @InjectMocks
    private ReservaService reservaService;

    private Reserva reserva;
    private ReservaDTO reservaDTO;
    private Partido partido;
    private Usuario usuario;
    private PartidosSeleccionadosDTO partidosSeleccionadosDTO;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test User");
        usuario.setEmail("test@test.com");

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
        partido.setPrecio(100.0);
        partido.setFechaHora(LocalDateTime.now().plusDays(1));

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setUsuario(usuario);
        reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
        reserva.setFechaCreacion(LocalDateTime.now());

        LineaReserva lineaReserva = new LineaReserva();
        lineaReserva.setId(1L);
        lineaReserva.setReserva(reserva);
        lineaReserva.setPartido(partido);
        lineaReserva.setCantidad(2);
        reserva.setLineasReserva(Arrays.asList(lineaReserva));

        reservaDTO = new ReservaDTO();
        reservaDTO.setId(1L);
        reservaDTO.setUsuarioId(1L);
        reservaDTO.setEstado(Reserva.EstadoReserva.PENDIENTE);

        partidosSeleccionadosDTO = new PartidosSeleccionadosDTO();
        partidosSeleccionadosDTO.setUsuarioId(1L);
        LineaPartidoSeleccionadoDTO linea = new LineaPartidoSeleccionadoDTO();
        linea.setPartidoId(1L);
        linea.setCantidad(2);
        partidosSeleccionadosDTO.setItems(Arrays.asList(linea));
    }

    @Test
    void obtenerTodos_ShouldReturnListOfReservas() {
        List<Reserva> reservas = Arrays.asList(reserva);
        when(reservaRepository.findAll()).thenReturn(reservas);
        when(mapperUtil.toReservaDTO(any(Reserva.class))).thenReturn(reservaDTO);

        List<ReservaDTO> result = reservaService.obtenerTodos();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnReserva() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(mapperUtil.toReservaDTO(reserva)).thenReturn(reservaDTO);

        ReservaDTO result = reservaService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(reservaRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_WithInvalidId_ShouldThrowException() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reservaService.obtenerPorId(999L);
        });
    }

    @Test
    void obtenerPorUsuario_ShouldReturnUserReservas() {
        List<Reserva> reservas = Arrays.asList(reserva);
        when(reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L)).thenReturn(reservas);
        when(mapperUtil.toReservaDTO(any(Reserva.class))).thenReturn(reservaDTO);

        List<ReservaDTO> result = reservaService.obtenerPorUsuario(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaRepository, times(1)).findByUsuarioIdOrderByFechaCreacionDesc(1L);
    }

    @Test
    void crearDesdePartidosSeleccionados_WithValidData_ShouldCreateReserva() {
        when(partidosSeleccionadosService.obtenerPartidosSeleccionadosPorUsuario(1L))
                .thenReturn(partidosSeleccionadosDTO);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(usuario);
        when(partidoService.obtenerPartidoEntity(1L)).thenReturn(partido);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        when(mapperUtil.toReservaDTO(any(Reserva.class))).thenReturn(reservaDTO);
        doNothing().when(partidosSeleccionadosService).vaciarPartidosSeleccionados(1L);

        ReservaDTO result = reservaService.crearDesdePartidosSeleccionados(1L);

        assertNotNull(result);
        verify(reservaRepository, atLeastOnce()).save(any(Reserva.class));
        verify(partidosSeleccionadosService, times(1)).vaciarPartidosSeleccionados(1L);
    }

    @Test
    void crearDesdePartidosSeleccionados_WithEmptyPartidos_ShouldThrowException() {
        partidosSeleccionadosDTO.setItems(new ArrayList<>());
        when(partidosSeleccionadosService.obtenerPartidosSeleccionadosPorUsuario(1L))
                .thenReturn(partidosSeleccionadosDTO);

        assertThrows(BusinessException.class, () -> {
            reservaService.crearDesdePartidosSeleccionados(1L);
        });
    }

    @Test
    void actualizarEstado_WithValidTransition_ShouldUpdateEstado() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        when(mapperUtil.toReservaDTO(any(Reserva.class))).thenReturn(reservaDTO);

        reservaDTO.setEstado(Reserva.EstadoReserva.CONFIRMADO);
        ReservaDTO result = reservaService.actualizarEstado(1L, Reserva.EstadoReserva.CONFIRMADO);

        assertNotNull(result);
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void actualizarEstado_WithInvalidTransition_ShouldThrowException() {
        reserva.setEstado(Reserva.EstadoReserva.FINALIZADO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThrows(BusinessException.class, () -> {
            reservaService.actualizarEstado(1L, Reserva.EstadoReserva.CONFIRMADO);
        });
    }

    @Test
    void cancelar_WithValidId_ShouldCancelReserva() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        reservaService.cancelar(1L);

        verify(reservaRepository, times(1)).save(any(Reserva.class));
        assertEquals(Reserva.EstadoReserva.CANCELADO, reserva.getEstado());
    }

    @Test
    void cancelar_WithFinalizedReserva_ShouldThrowException() {
        reserva.setEstado(Reserva.EstadoReserva.FINALIZADO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThrows(BusinessException.class, () -> {
            reservaService.cancelar(1L);
        });
    }

    @Test
    void calcularTotalGastadoPorUsuario_ShouldReturnTotal() {
        reserva.setEstado(Reserva.EstadoReserva.CONFIRMADO);
        List<Reserva> reservas = Arrays.asList(reserva);
        when(reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L)).thenReturn(reservas);

        Double result = reservaService.calcularTotalGastadoPorUsuario(1L);

        assertNotNull(result);
        verify(reservaRepository, times(1)).findByUsuarioIdOrderByFechaCreacionDesc(1L);
    }
}

