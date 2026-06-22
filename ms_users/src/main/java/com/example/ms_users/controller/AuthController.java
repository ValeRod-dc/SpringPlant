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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación y Registro", description = "Endpoints públicos para autenticación y registro de usuarios")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;

    // ========== ENDPOINTS PÚBLICOS ==========

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nueva cuenta de usuario con rol, email y teléfono.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Username o email ya existente")
    })
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDTO userRequest) {
        log.info("Intentando registrar usuario: {}", userRequest.getUsername());

        String roleStr = userRequest.getRole() != null ? userRequest.getRole().toUpperCase() : "CLIENT";

        if (!roleStr.equals("ADMIN") && !roleStr.equals("EMPLOYEE") && !roleStr.equals("CLIENT")) {
            log.warn("Registro fallido - Rol inválido: {} para usuario: {}", roleStr, userRequest.getUsername());
            throw new InvalidRoleException("Rol inválido. Use: ADMIN, EMPLOYEE o CLIENT");
        }

        if (userService.existsByUsername(userRequest.getUsername())) {
            log.warn("Registro fallido - Username ya existe: {}", userRequest.getUsername());
            throw new UsernameAlreadyExistsException("El username '" + userRequest.getUsername() + "' ya está en uso");
        }

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
                userRequest.getPhone(),
                userRequest.getAddress()
        );

        UserResponseDTO response = mapToResponseDTO(user);
        addAuthLinks(response, user.getUsername());

        log.info("Usuario registrado con éxito: {} (rol: {})", user.getUsername(), user.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica usuario y retorna token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login existoso, retorna token"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
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
    @Operation(summary = "Validar token", description = "Verificar si el token JWT es válido (se envía en el header Authorization).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido o ausente")
    })
    public ResponseEntity<?> validateToken() {
        log.debug("Validación de token solicitada");
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @GetMapping("/user-exists/{username}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta con campo 'exists'")
    })
    public ResponseEntity<?> userExists(@Parameter(description = "Nombre de usuario a verificar", example = "Fernando Carnaca Supremo I") @PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        log.debug("Verificando existencia de usuario: {} -> {}", username, exists);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ========== ENDPOINTS PARA USUARIOS AUTENTICADOS ==========

    @GetMapping("/user/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Obtener perfil propio", description = "Retorna los datos del usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
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

        UserResponseDTO response = mapToResponseDTO(user);
        addAuthLinks(response, username);

        return ResponseEntity.ok(Map.of("user", response));
    }

    @GetMapping("/user-id/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener ID de usuario por su username", description = "Retorna el ID del usuario (endpoint para servicios internos).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ID retornado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado: ")
    })
    public ResponseEntity<?> getUserIdByUsername(@Parameter(description = "Nombre de usuario", example = "Fernando Carnaca Supremo I") @PathVariable String username) {
        log.debug("ADMIN - Obteniendo ID de usuario: {}", username);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));
        return ResponseEntity.ok(Map.of("userId", user.getUserId()));
    }

    // ========== METODOS UTILITARIOS ==========

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private void addAuthLinks(UserResponseDTO dto, String username) {
        dto.add(linkTo(methodOn(AuthController.class).getMyProfile(null)).withRel("profile"));
        dto.add(linkTo(methodOn(UserController.class).updateMyProfile(null, null)).withRel("update-profile"));
        dto.add(linkTo(methodOn(UserController.class).deleteOwnAccount(null)).withRel("delete-account"));
        dto.add(linkTo(methodOn(AdminController.class).getUserByUsername(username)).withRel("admin-details"));
    }
}