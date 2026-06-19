package com.example.ms_users.controller;

import com.example.ms_users.dto.request.UserUpdateDTO;
import com.example.ms_users.dto.response.UserResponseDTO;
import com.example.ms_users.exception.custom.InvalidRoleException;
import com.example.ms_users.exception.custom.UserNotFoundException;
import com.example.ms_users.model.Role;
import com.example.ms_users.model.User;
import com.example.ms_users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administración de Usuarios", description = "Endpoints exclusivos para administradores (ADMIN)")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Retorna todos los usuarios registrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (no ADMIN)")
    })
    public ResponseEntity<?> getAllUsers() {
        log.info("ADMIN - Solicitud de listar todos los usuarios");

        List<User> users = userService.findAll();
        List<UserResponseDTO> userResponses = users.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        log.debug("ADMIN - Total de usuarios encontrados: {}", userResponses.size());
        return ResponseEntity.ok(Map.of(
                "users", userResponses,
                "total", userResponses.size()
        ));
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por su username", description = "Retorna los datos de un usuario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> getUserByUsername(@Parameter(description = "Nombre de usuario", example = "Fernando Carnaca Supremo I")
                                               @PathVariable String username) {
        log.info("ADMIN - Solicitando información del usuario: {}", username);

        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));

            UserResponseDTO response = mapToResponseDTO(user);
            log.debug("ADMIN - Usuario encontrado: {}", username);
            return ResponseEntity.ok(Map.of("user", response));

        } catch (UserNotFoundException e) {
            log.warn("ADMIN - Usuario no encontrado: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuarios por rol", description = "Retorna todos los usuarios con un rol específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista filtrada"),
            @ApiResponse(responseCode = "400", description = "Rol inválido")
    })
    public ResponseEntity<?> getUsersByRole(@Parameter(description = "Rol (ADMIN, EMPLOYEE, CLIENT)", example = "CLIENT")
                                            @PathVariable String role) {
        log.info("ADMIN - Solicitando usuarios por rol: {}", role);

        try {
            Role validRole = Role.valueOf(role.toUpperCase());
            List<User> users = userService.findAll().stream()
                    .filter(user -> user.getRole() == validRole)
                    .collect(Collectors.toList());

            List<UserResponseDTO> userResponses = users.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());

            log.debug("ADMIN - Usuarios con rol {} encontrados: {}", role, userResponses.size());
            return ResponseEntity.ok(Map.of(
                    "role", role,
                    "users", userResponses,
                    "total", userResponses.size()
            ));

        } catch (IllegalArgumentException e) {
            log.warn("ADMIN - Rol inválido solicitado: {}", role);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Rol inválido. Use: ADMIN, EMPLOYEE o CLIENT"));
        }
    }

    @GetMapping("{id}")
    @Operation(summary = "Obtener usuario por ID (con enlaces HATEOAS)", description = "Retorna el usuario con enlaces HAL.????????")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<User> findById(@Parameter(description = "ID del usuario", example = "1")
                                         @PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if(!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        //user.add(linkTo(methodOn(AdminController.class).findAll()).withRel("todos"));
        user.add(linkTo(methodOn(AdminController.class)
                .findById(user.getUserId())).withSelfRel());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar usuario por admin", description = "Permite a un administrador modificar los datos de cualquier usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> updateUserByAdmin(@Parameter(description = "Nombre de usuario", example = "Fernando Carnaca Supremo I")
                                               @PathVariable String username,
                                               @Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("ADMIN - Actualizando usuario: {}", username);

        try {
            User updatedUser = userService.updateUserProfile(username, updateDTO);
            UserResponseDTO response = mapToResponseDTO(updatedUser);

            log.info("ADMIN - Usuario actualizado exitosamente: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario actualizado correctamente por ADMIN",
                    "user", response
            ));

        } catch (UserNotFoundException e) {
            log.error("ADMIN - Usuario no encontrado: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("ADMIN - Error actualizando usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario por un admin", description = "Elimina la cuenta de un usuario (solo ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> deleteUserByUsername(@Parameter(description = "Nombre de usuario", example = "Fernando Carnaca Supremo I")
                                                  @PathVariable String username) {
        log.warn("ADMIN - Eliminando usuario: {}", username);

        try {
            userService.deleteByUsername(username);
            log.info("ADMIN - Usuario eliminado exitosamente: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario eliminado correctamente",
                    "username", username
            ));

        } catch (UserNotFoundException e) {
            log.error("ADMIN - Usuario no encontrado: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("ADMIN - Error eliminando usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar el usuario"));
        }
    }

    @PatchMapping("/users/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar el rol de un usuario", description = "Modifica el rol de un usuario (solo ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol actualizado"),
            @ApiResponse(responseCode = "400", description = "Rol inválido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> changeUserRole(@Parameter(description = "Nombre de usuario", example = "Fernando Carnaca Supremo I")
                                            @PathVariable String username,
                                            @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        log.info("ADMIN - Cambiando rol del usuario {} a: {}", username, newRole);

        if (newRole == null || newRole.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El rol es requerido"));
        }

        try {
            Role role = Role.valueOf(newRole.toUpperCase());
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));

            user.setRole(role);
            User updatedUser = userService.save(user);
            UserResponseDTO response = mapToResponseDTO(updatedUser);

            log.info("ADMIN - Rol actualizado exitosamente para: {} -> {}", username, newRole);
            return ResponseEntity.ok(Map.of(
                    "message", "Rol actualizado correctamente",
                    "user", response
            ));

        } catch (IllegalArgumentException e) {
            log.error("ADMIN - Rol inválido: {}", newRole);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Rol inválido. Use: ADMIN, EMPLOYEE o CLIENT"));
        } catch (UserNotFoundException e) {
            log.error("ADMIN - Usuario no encontrado: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/search/email")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuario por email", description = "Retorna un usuario por su email exacto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> searchUserByEmail(@Parameter(description = "Correo electrónico", example = "fer_carnaca@gmail.com")
                                               @RequestParam String email) {
        log.info("ADMIN - Buscando usuario por email: {}", email);

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + email));

            UserResponseDTO response = mapToResponseDTO(user);
            log.debug("ADMIN - Usuario encontrado por email: {}", email);
            return ResponseEntity.ok(Map.of("user", response));

        } catch (UserNotFoundException e) {
            log.warn("ADMIN - Usuario no encontrado por email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== METODO UTILITARIO ==========

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .address(user.getAddress())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}