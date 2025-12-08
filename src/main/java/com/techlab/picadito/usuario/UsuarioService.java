package com.techlab.picadito.usuario;

import com.techlab.picadito.dto.UsuarioDTO;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    
    public UsuarioDTO obtenerPorId(Long id) {
        Objects.requireNonNull(id, "El ID del usuario no puede ser null");
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return toUsuarioDTO(usuario);
    }
    
    public Usuario obtenerUsuarioEntity(Long id) {
        Objects.requireNonNull(id, "El ID del usuario no puede ser null");
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }
    
    private UsuarioDTO toUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.getActivo());
        return dto;
    }
}

