package com.techlab.picadito.service;

import com.techlab.picadito.dto.UsuarioDTO;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.model.Usuario.RolUsuario;
import com.techlab.picadito.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private com.techlab.picadito.usuario.UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test User");
        usuario.setEmail("test@example.com");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setActivo(true);
    }

    @Test
    void obtenerPorId_WithValidId_ShouldReturnUsuarioDTO() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioDTO result = usuarioService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getNombre());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(RolUsuario.CLIENTE, result.getRol());
        assertTrue(result.getActivo());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_WithInvalidId_ShouldThrowException() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.obtenerPorId(999L);
        });
    }

    @Test
    void obtenerPorId_WithNullId_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            usuarioService.obtenerPorId(null);
        });
    }

    @Test
    void obtenerUsuarioEntity_WithValidId_ShouldReturnUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.obtenerUsuarioEntity(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getNombre());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerUsuarioEntity_WithInvalidId_ShouldThrowException() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.obtenerUsuarioEntity(999L);
        });
    }

    @Test
    void obtenerUsuarioEntity_WithNullId_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            usuarioService.obtenerUsuarioEntity(null);
        });
    }
}


