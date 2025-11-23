package com.techlab.picadito.service;

import com.techlab.picadito.dto.SedeDTO;
import com.techlab.picadito.dto.SedeResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Sede;
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
@SuppressWarnings("null")
class SedeServiceTest {

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private PartidoRepository partidoRepository;

    @InjectMocks
    private SedeService sedeService;

    private Sede sede;
    private SedeDTO sedeDTO;
    private SedeResponseDTO sedeResponseDTO;

    @BeforeEach
    void setUp() {
        sede = new Sede();
        sede.setId(1L);
        sede.setNombre("Estadio Olímpico");
        sede.setDireccion("Av. Principal 123");
        sede.setDescripcion("Estadio moderno");
        sede.setTelefono("1234567890");
        sede.setFechaCreacion(LocalDateTime.now());

        sedeDTO = new SedeDTO();
        sedeDTO.setNombre("Estadio Olímpico");
        sedeDTO.setDireccion("Av. Principal 123");
        sedeDTO.setDescripcion("Estadio moderno");
        sedeDTO.setTelefono("1234567890");

        sedeResponseDTO = new SedeResponseDTO();
        sedeResponseDTO.setId(1L);
        sedeResponseDTO.setNombre("Estadio Olímpico");
        sedeResponseDTO.setDireccion("Av. Principal 123");
    }

    @Test
    void obtenerTodas_ShouldReturnAllSedes() {
        List<Sede> sedes = Arrays.asList(sede);
        when(sedeRepository.findAll()).thenReturn(sedes);

        List<SedeResponseDTO> result = sedeService.obtenerTodas();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sedeRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnSede() {
        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));

        SedeResponseDTO result = sedeService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(sedeRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_WithInvalidId_ShouldThrowException() {
        when(sedeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            sedeService.obtenerPorId(999L);
        });
    }

    @Test
    void crear_WithValidData_ShouldCreateSede() {
        when(sedeRepository.findByNombreIgnoreCase("Estadio Olímpico"))
                .thenReturn(Optional.empty());
        when(sedeRepository.save(any(Sede.class))).thenReturn(sede);

        SedeResponseDTO result = sedeService.crear(sedeDTO);

        assertNotNull(result);
        verify(sedeRepository, times(1)).save(any(Sede.class));
    }

    @Test
    void crear_WithDuplicateName_ShouldThrowException() {
        when(sedeRepository.findByNombreIgnoreCase("Estadio Olímpico"))
                .thenReturn(Optional.of(sede));

        assertThrows(BusinessException.class, () -> {
            sedeService.crear(sedeDTO);
        });
    }

    @Test
    void actualizar_WithValidData_ShouldUpdateSede() {
        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(sedeRepository.findByNombreIgnoreCase("Estadio Olímpico"))
                .thenReturn(Optional.of(sede));
        when(sedeRepository.save(any(Sede.class))).thenReturn(sede);

        SedeResponseDTO result = sedeService.actualizar(1L, sedeDTO);

        assertNotNull(result);
        verify(sedeRepository, times(1)).save(any(Sede.class));
    }

    @Test
    void actualizar_WithDuplicateName_ShouldThrowException() {
        Sede otraSede = new Sede();
        otraSede.setId(2L);
        otraSede.setNombre("Estadio Olímpico");

        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(sedeRepository.findByNombreIgnoreCase("Estadio Olímpico"))
                .thenReturn(Optional.of(otraSede));

        assertThrows(BusinessException.class, () -> {
            sedeService.actualizar(1L, sedeDTO);
        });
    }

    @Test
    void eliminar_WithValidId_ShouldDeleteSede() {
        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(partidoRepository.findAll()).thenReturn(Arrays.asList());

        sedeService.eliminar(1L);

        verify(sedeRepository, times(1)).delete(any(Sede.class));
    }

    @Test
    void eliminar_WithPartidosAsociados_ShouldThrowException() {
        Partido partido = new Partido();
        partido.setSede(sede);

        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(partidoRepository.findAll()).thenReturn(Arrays.asList(partido));

        assertThrows(BusinessException.class, () -> {
            sedeService.eliminar(1L);
        });
    }
}

