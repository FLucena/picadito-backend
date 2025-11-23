package com.techlab.picadito.service;

import com.techlab.picadito.dto.ParticipanteDTO;
import com.techlab.picadito.dto.ParticipanteResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Nivel;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Participante;
import com.techlab.picadito.model.Posicion;
import com.techlab.picadito.repository.ParticipanteRepository;
import com.techlab.picadito.repository.PartidoRepository;
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
class ParticipanteServiceTest {

    @Mock
    private ParticipanteRepository participanteRepository;

    @Mock
    private PartidoRepository partidoRepository;

    @Mock
    private PartidoService partidoService;

    @InjectMocks
    private ParticipanteService participanteService;

    private Partido partido;
    private Participante participante;
    private ParticipanteDTO participanteDTO;

    @BeforeEach
    void setUp() {
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
        partido.setParticipantes(new ArrayList<>());

        participante = new Participante();
        participante.setId(1L);
        participante.setNombre("Juan Pérez");
        participante.setApodo("Juancho");
        participante.setPosicion(Posicion.DELANTERO);
        participante.setNivel(Nivel.INTERMEDIO);
        participante.setPartido(partido);

        participanteDTO = new ParticipanteDTO();
        participanteDTO.setNombre("Juan Pérez");
        participanteDTO.setApodo("Juancho");
        participanteDTO.setPosicion(Posicion.DELANTERO);
        participanteDTO.setNivel(Nivel.INTERMEDIO);
    }

    @Test
    @SuppressWarnings("null")
    void inscribirseAPartido_WithValidData_ShouldCreateParticipante() {
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        when(participanteRepository.existsByPartidoAndNombre(partido, "Juan Pérez")).thenReturn(false);
        when(participanteRepository.save(any(Participante.class))).thenReturn(participante);
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        doNothing().when(partidoService).actualizarEstadoSegunParticipantes(any(Partido.class));

        ParticipanteResponseDTO result = participanteService.inscribirseAPartido(1L, participanteDTO);

        assertNotNull(result);
        assertEquals("Juan Pérez", result.getNombre());
        verify(participanteRepository, times(1)).save(any(Participante.class));
    }

    @Test
    void inscribirseAPartido_WhenPartidoNotAvailable_ShouldThrowException() {
        partido.setEstado(EstadoPartido.COMPLETO);
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));

        assertThrows(BusinessException.class, () -> {
            participanteService.inscribirseAPartido(1L, participanteDTO);
        });
    }

    @Test
    void inscribirseAPartido_WhenPartidoCompleto_ShouldThrowException() {
        // cantidadParticipantes es calculado, así que agregamos participantes hasta completar
        for (int i = 0; i < 10; i++) {
            Participante p = new Participante();
            p.setId((long) i);
            p.setNombre("Participante " + i);
            p.setPartido(partido);
            partido.getParticipantes().add(p);
        }
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));

        assertThrows(BusinessException.class, () -> {
            participanteService.inscribirseAPartido(1L, participanteDTO);
        });
    }

    @Test
    void inscribirseAPartido_WithDuplicateName_ShouldThrowException() {
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        when(participanteRepository.existsByPartidoAndNombre(partido, "Juan Pérez")).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            participanteService.inscribirseAPartido(1L, participanteDTO);
        });
    }

    @Test
    void obtenerParticipantesPorPartido_WithValidId_ShouldReturnList() {
        List<Participante> participantes = Arrays.asList(participante);
        when(partidoRepository.existsById(1L)).thenReturn(true);
        when(participanteRepository.findByPartidoId(1L)).thenReturn(participantes);

        List<ParticipanteResponseDTO> result = participanteService.obtenerParticipantesPorPartido(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(participanteRepository, times(1)).findByPartidoId(1L);
    }

    @Test
    void obtenerParticipantesPorPartido_WithInvalidId_ShouldThrowException() {
        when(partidoRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            participanteService.obtenerParticipantesPorPartido(999L);
        });
    }

    @Test
    @SuppressWarnings("null")
    void desinscribirseDePartido_WithValidIds_ShouldRemoveParticipante() {
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        when(participanteRepository.findById(1L)).thenReturn(Optional.of(participante));
        doNothing().when(participanteRepository).delete(any(Participante.class));
        doNothing().when(partidoService).actualizarEstadoSegunParticipantes(any(Partido.class));

        participanteService.desinscribirseDePartido(1L, 1L);

        verify(participanteRepository, times(1)).delete(any(Participante.class));
        verify(partidoService, times(1)).actualizarEstadoSegunParticipantes(any(Partido.class));
    }

    @Test
    void desinscribirseDePartido_WithInvalidPartidoId_ShouldThrowException() {
        when(partidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            participanteService.desinscribirseDePartido(999L, 1L);
        });
    }

    @Test
    void desinscribirseDePartido_WithInvalidParticipanteId_ShouldThrowException() {
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        when(participanteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            participanteService.desinscribirseDePartido(1L, 999L);
        });
    }

    @Test
    void desinscribirseDePartido_WhenParticipanteNotBelongsToPartido_ShouldThrowException() {
        Partido otroPartido = new Partido();
        otroPartido.setId(2L);
        participante.setPartido(otroPartido);

        when(partidoRepository.findById(1L)).thenReturn(Optional.of(partido));
        when(participanteRepository.findById(1L)).thenReturn(Optional.of(participante));

        assertThrows(BusinessException.class, () -> {
            participanteService.desinscribirseDePartido(1L, 1L);
        });
    }
}

