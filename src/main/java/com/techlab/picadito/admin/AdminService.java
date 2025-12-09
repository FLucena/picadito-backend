package com.techlab.picadito.admin;

import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.dto.PartidosResponseDTO;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.partido.PartidoRepository;
import com.techlab.picadito.partido.PartidoService;
import com.techlab.picadito.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final PartidoRepository partidoRepository;
    private final PartidoService partidoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Obtiene partidos con capacidad disponible baja (equivalente a stock bajo)
     * 
     * @param capacidadMinima Capacidad mínima disponible para considerar un partido como crítico (default: 5)
     * @return Lista de partidos con capacidad baja, ordenados por capacidad disponible ascendente
     */
    public PartidosResponseDTO obtenerPartidosConCapacidadBaja(Integer capacidadMinima) {
        final int capacidadMinimaFinal = (capacidadMinima == null) ? 5 : capacidadMinima;
        
        List<Partido> partidosDisponibles = partidoRepository.findByEstado(EstadoPartido.DISPONIBLE);
        
        // Filtrar partidos con capacidad disponible <= capacidadMinima
        // y ordenar por capacidad disponible ascendente (más críticos primero)
        List<PartidoResponseDTO> partidos = partidosDisponibles.stream()
                .filter(partido -> {
                    int capacidadDisponible = partido.getMaxJugadores() - partido.getCantidadParticipantes();
                    return capacidadDisponible <= capacidadMinimaFinal;
                })
                .sorted(Comparator.comparingInt(partido -> 
                    partido.getMaxJugadores() - partido.getCantidadParticipantes()))
                .map(partido -> {
                    Long partidoId = Objects.requireNonNull(partido.getId(), "El ID del partido no puede ser null");
                    return partidoService.obtenerPartidoPorId(partidoId);
                })
                .collect(Collectors.toList());
        return new PartidosResponseDTO(partidos);
    }
    
    /**
     * Cambia la contraseña de un usuario por su ID
     * 
     * @param usuarioId ID del usuario
     * @param nuevaPassword Nueva contraseña en texto plano (será hasheada automáticamente)
     */
    @Transactional
    public void cambiarPasswordUsuario(Long usuarioId, String nuevaPassword) {
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser null");
        Objects.requireNonNull(nuevaPassword, "La nueva contraseña no puede ser null");
        
        if (nuevaPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
        
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }
    
    /**
     * Cambia la contraseña de un usuario por su email
     * 
     * @param email Email del usuario
     * @param nuevaPassword Nueva contraseña en texto plano (será hasheada automáticamente)
     */
    @Transactional
    public void cambiarPasswordUsuarioPorEmail(String email, String nuevaPassword) {
        Objects.requireNonNull(email, "El email no puede ser null");
        Objects.requireNonNull(nuevaPassword, "La nueva contraseña no puede ser null");
        
        if (nuevaPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }
}

