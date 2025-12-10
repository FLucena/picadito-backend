package com.techlab.picadito.security;

import com.techlab.picadito.dto.AuthResponseDTO;
import com.techlab.picadito.dto.LoginRequestDTO;
import com.techlab.picadito.dto.RefreshTokenRequestDTO;
import com.techlab.picadito.dto.RegisterRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        AuthResponseDTO response = authService.register(request, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        AuthResponseDTO response = authService.login(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}

