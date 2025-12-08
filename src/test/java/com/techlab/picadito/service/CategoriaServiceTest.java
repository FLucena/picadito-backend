package com.techlab.picadito.service;

import com.techlab.picadito.dto.CategoriaDTO;
import com.techlab.picadito.dto.CategoriaResponseDTO;
import com.techlab.picadito.dto.CategoriasResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Categoria;
import com.techlab.picadito.categoria.CategoriaRepository;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private com.techlab.picadito.categoria.CategoriaService categoriaService;

    private Categoria categoria;
    private CategoriaDTO categoriaDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Fútbol 11");
        categoria.setDescripcion("Partidos de fútbol 11");
        categoria.setIcono("football");
        categoria.setColor("#FF0000");

        categoriaDTO = new CategoriaDTO();
        categoriaDTO.setNombre("Fútbol 11");
        categoriaDTO.setDescripcion("Partidos de fútbol 11");
        categoriaDTO.setIcono("football");
        categoriaDTO.setColor("#FF0000");
    }

    @Test
    void obtenerTodas_ShouldReturnListOfCategorias() {
        List<Categoria> categorias = Arrays.asList(categoria);
        when(categoriaRepository.findAllByOrderByNombreAsc()).thenReturn(categorias);

        CategoriasResponseDTO result = categoriaService.obtenerTodas();

        assertNotNull(result);
        assertNotNull(result.getCategorias());
        assertEquals(1, result.getCategorias().size());
        assertEquals("Fútbol 11", result.getCategorias().get(0).getNombre());
        assertEquals(1, result.getTotal());
        verify(categoriaRepository, times(1)).findAllByOrderByNombreAsc();
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        CategoriaResponseDTO result = categoriaService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fútbol 11", result.getNombre());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_WithInvalidId_ShouldThrowException() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoriaService.obtenerPorId(999L);
        });
    }

    @Test
    void crear_WithValidData_ShouldCreateCategoria() {
        when(categoriaRepository.existsByNombre("Fútbol 11")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaResponseDTO result = categoriaService.crear(categoriaDTO);

        assertNotNull(result);
        assertEquals("Fútbol 11", result.getNombre());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void crear_WithDuplicateName_ShouldThrowException() {
        when(categoriaRepository.existsByNombre("Fútbol 11")).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            categoriaService.crear(categoriaDTO);
        });
    }

    @Test
    void actualizar_WithValidData_ShouldUpdateCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaResponseDTO result = categoriaService.actualizar(1L, categoriaDTO);

        assertNotNull(result);
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void actualizar_WithInvalidId_ShouldThrowException() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoriaService.actualizar(999L, categoriaDTO);
        });
    }

    @Test
    void actualizar_WithDuplicateName_ShouldThrowException() {
        Categoria otraCategoria = new Categoria();
        otraCategoria.setId(2L);
        otraCategoria.setNombre("Otra Categoría");

        categoriaDTO.setNombre("Otra Categoría");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombre("Otra Categoría")).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            categoriaService.actualizar(1L, categoriaDTO);
        });
    }

    @Test
    void eliminar_WithValidId_ShouldDeleteCategoria() {
        when(categoriaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoriaRepository).deleteById(1L);

        categoriaService.eliminar(1L);

        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_WithInvalidId_ShouldThrowException() {
        when(categoriaRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            categoriaService.eliminar(999L);
        });
    }

    @Test
    void obtenerCategoriaEntity_WithValidId_ShouldReturnCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria result = categoriaService.obtenerCategoriaEntity(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fútbol 11", result.getNombre());
    }

    @Test
    void obtenerCategoriaEntity_WithInvalidId_ShouldThrowException() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoriaService.obtenerCategoriaEntity(999L);
        });
    }
}


