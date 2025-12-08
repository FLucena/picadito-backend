package com.techlab.picadito.security;

import com.techlab.picadito.dto.AuthResponseDTO;
import com.techlab.picadito.dto.LoginRequestDTO;
import com.techlab.picadito.dto.RefreshTokenRequestDTO;
import com.techlab.picadito.dto.RegisterRequestDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Usuario.RolUsuario.CLIENTE);
        usuario.setActivo(true);

        usuario = usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .message("Usuario registrado exitosamente")
                .build();
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        if (!usuario.getActivo()) {
            throw new BusinessException("Usuario inactivo");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .message("Login exitoso")
                .build();
    }
    
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        if (!jwtService.validateRefreshToken(request.getRefreshToken())) {
            throw new BusinessException("Refresh token inválido o expirado");
        }
        
        String email = jwtService.extractUsername(request.getRefreshToken());
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        
        if (!usuario.getActivo()) {
            throw new BusinessException("Usuario inactivo");
        }
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String newToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        
        return AuthResponseDTO.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .message("Token renovado exitosamente")
                .build();
    }
}

