package com.example.ms_users.controller;

import com.example.ms_users.dto.request.AuthRequestDTO;
import com.example.ms_users.dto.request.UserRequestDTO;
import com.example.ms_users.dto.response.UserResponseDTO;
import com.example.ms_users.exception.custom.EmailAlreadyExistsException;
import com.example.ms_users.exception.custom.InvalidRoleException;
import com.example.ms_users.exception.custom.UserNotFoundException;
import com.example.ms_users.exception.custom.UsernameAlreadyExistsException;
import com.example.ms_users.model.Role;
import com.example.ms_users.model.User;
import com.example.ms_users.security.jwt.JwtService;
import com.example.ms_users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;

    // ========== ENDPOINTS PÚBLICOS ==========

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDTO userRequest) {
        log.info("Intentando registrar usuario: {}", userRequest.getUsername());

        // Validar rol (solo ADMIN, EMPLOYEE o CLIENT)
        String roleStr = userRequest.getRole() != null ? userRequest.getRole().toUpperCase() : "CLIENT";

        // Validar que el rol sea permitido
        if (!roleStr.equals("ADMIN") && !roleStr.equals("EMPLOYEE") && !roleStr.equals("CLIENT")) {
            log.warn("Registro fallido - Rol inválido: {} para usuario: {}", roleStr, userRequest.getUsername());
            throw new InvalidRoleException("Rol inválido. Use: ADMIN, EMPLOYEE o CLIENT");
        }

        // Verificar si el username ya existe
        if (userService.existsByUsername(userRequest.getUsername())) {
            log.warn("Registro fallido - Username ya existe: {}", userRequest.getUsername());
            throw new UsernameAlreadyExistsException("El username '" + userRequest.getUsername() + "' ya está en uso");
        }

        // Verificar si el email ya existe
        if (userService.existsByEmail(userRequest.getEmail())) {
            log.warn("Registro fallido - Email ya existe: {}", userRequest.getEmail());
            throw new EmailAlreadyExistsException("El email '" + userRequest.getEmail() + "' ya está registrado");
        }

        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            role = Role.CLIENT;
        }

        User user = userService.register(
                userRequest.getUsername(),
                userRequest.getPassword(),
                role,
                userRequest.getEmail(),
                userRequest.getPhone()
        );

        UserResponseDTO response = UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();

        log.info("Usuario registrado con éxito: {} (rol: {})", user.getUsername(), user.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO authRequest) {
        log.info("Intento de login para: {}", authRequest.getUsername());

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            if (auth.isAuthenticated()) {
                User user = userService.findByUsername(authRequest.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
                String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

                log.info("Login exitoso para: {} (rol: {})", user.getUsername(), user.getRole().name());

                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "username", user.getUsername(),
                        "role", user.getRole().name(),
                        "email", user.getEmail(),
                        "phone", user.getPhone() != null ? user.getPhone() : "",
                        "message", "Login exitoso"
                ));
            }

            log.warn("Login fallido - Usuario no autenticado: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));

        } catch (BadCredentialsException e) {
            log.warn("Login fallido - Credenciales incorrectas para: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrectos"));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        log.debug("Validación de token solicitada");
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @GetMapping("/user-exists/{username}")
    public ResponseEntity<?> userExists(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        log.debug("Verificando existencia de usuario: {} -> {}", username, exists);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ========== ENDPOINTS PARA USUARIOS AUTENTICADOS ==========

    @GetMapping("/user/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de obtener perfil - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Obteniendo perfil de usuario: {}", username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));

        UserResponseDTO response = UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(Map.of("user", response));
    }
}