package com.techlab.picadito.security;

import com.techlab.picadito.dto.AuthResponseDTO;
import com.techlab.picadito.dto.LoginRequestDTO;
import com.techlab.picadito.dto.RefreshTokenRequestDTO;
import com.techlab.picadito.dto.RegisterRequestDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordValidator passwordValidator;
    private final RateLimitingService rateLimitingService;
    private final AuthenticationAuditService auditService;

    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.security.account-lockout-duration-minutes:30}")
    private int accountLockoutDurationMinutes;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request, String ipAddress) {
        // Validar contraseña con políticas de seguridad
        try {
            passwordValidator.validate(request.getPassword());
        } catch (IllegalArgumentException e) {
            auditService.logFailedRegistration(request.getEmail(), ipAddress, e.getMessage());
            throw new BusinessException(e.getMessage());
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            auditService.logFailedRegistration(request.getEmail(), ipAddress, "Email ya registrado");
            throw new BusinessException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Usuario.RolUsuario.CLIENTE);
        usuario.setActivo(true);
        usuario.setIntentosFallidos(0);
        usuario.setCuentaBloqueada(false);

        usuario = usuarioRepository.save(usuario);

        auditService.logSuccessfulRegistration(usuario.getEmail(), ipAddress);

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

    public AuthResponseDTO login(LoginRequestDTO request, String ipAddress) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Verificar si la cuenta está bloqueada
        if (usuario != null && usuario.getCuentaBloqueada()) {
            if (usuario.getFechaBloqueo() != null) {
                LocalDateTime unlockTime = usuario.getFechaBloqueo()
                    .plusMinutes(accountLockoutDurationMinutes);
                
                if (LocalDateTime.now().isBefore(unlockTime)) {
                    long minutesRemaining = java.time.Duration.between(
                        LocalDateTime.now(), unlockTime).toMinutes();
                    auditService.logFailedLogin(request.getEmail(), ipAddress, 
                        "Cuenta bloqueada. Intenta en " + minutesRemaining + " minutos");
                    throw new BusinessException(
                        "Cuenta bloqueada por múltiples intentos fallidos. " +
                        "Intenta nuevamente en " + minutesRemaining + " minutos"
                    );
                } else {
                    // Desbloquear cuenta si ya pasó el tiempo
                    unlockAccount(usuario);
                }
            } else {
                // Si no hay fecha de bloqueo, desbloquear
                unlockAccount(usuario);
            }
        }

        // Verificar rate limiting por email
        if (!rateLimitingService.tryConsumeLoginAttempt(request.getEmail())) {
            auditService.logRateLimitExceeded("/api/auth/login", request.getEmail(), "EMAIL");
            auditService.logFailedLogin(request.getEmail(), ipAddress, "Rate limit excedido");
            throw new BusinessException("Demasiados intentos de login. Por favor, intenta más tarde.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Incrementar intentos fallidos
            if (usuario != null) {
                handleFailedLogin(usuario, ipAddress);
            } else {
                auditService.logFailedLogin(request.getEmail(), ipAddress, "Credenciales inválidas");
            }
            throw new BusinessException("Credenciales inválidas");
        }

        // Si llegamos aquí, el login fue exitoso
        if (usuario == null) {
            usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        }

        // Resetear intentos fallidos y desbloquear si estaba bloqueada
        boolean wasLocked = usuario.getCuentaBloqueada();
        if (usuario.getIntentosFallidos() > 0 || wasLocked) {
            usuario.setIntentosFallidos(0);
            usuario.setCuentaBloqueada(false);
            usuario.setFechaBloqueo(null);
            usuarioRepository.save(usuario);
            if (wasLocked) {
                auditService.logAccountUnlocked(usuario.getEmail());
            }
        }

        if (!usuario.getActivo()) {
            auditService.logFailedLogin(request.getEmail(), ipAddress, "Usuario inactivo");
            throw new BusinessException("Usuario inactivo");
        }

        auditService.logSuccessfulLogin(usuario.getEmail(), ipAddress);

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

    /**
     * Maneja un intento de login fallido
     */
    private void handleFailedLogin(Usuario usuario, String ipAddress) {
        int newAttempts = (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()) + 1;
        usuario.setIntentosFallidos(newAttempts);

        if (newAttempts >= maxLoginAttempts) {
            usuario.setCuentaBloqueada(true);
            usuario.setFechaBloqueo(LocalDateTime.now());
            auditService.logAccountLocked(usuario.getEmail(), ipAddress, newAttempts);
        }

        usuarioRepository.save(usuario);
        auditService.logFailedLogin(usuario.getEmail(), ipAddress, 
            "Intento fallido " + newAttempts + "/" + maxLoginAttempts);
    }

    /**
     * Desbloquea una cuenta
     */
    private void unlockAccount(Usuario usuario) {
        usuario.setCuentaBloqueada(false);
        usuario.setIntentosFallidos(0);
        usuario.setFechaBloqueo(null);
        usuarioRepository.save(usuario);
        auditService.logAccountUnlocked(usuario.getEmail());
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

