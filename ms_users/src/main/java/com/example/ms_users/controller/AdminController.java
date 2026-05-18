package com.example.ms_users.controller;

import com.example.ms_users.dto.request.UserUpdateDTO;
import com.example.ms_users.dto.response.UserResponseDTO;
import com.example.ms_users.exception.custom.InvalidRoleException;
import com.example.ms_users.exception.custom.UserNotFoundException;
import com.example.ms_users.model.Role;
import com.example.ms_users.model.User;
import com.example.ms_users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // ========== LISTAR TODOS LOS USUARIOS ==========

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
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

    // ========== OBTENER USUARIO POR USERNAME ==========

    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
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

    // ========== BUSCAR USUARIOS POR ROL ==========

    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
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

    // ========== ACTUALIZAR USUARIO (ADMIN) ==========

    @PutMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserByAdmin(@PathVariable String username,
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

    // ========== ELIMINAR USUARIO (ADMIN) ==========

    @DeleteMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserByUsername(@PathVariable String username) {
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

    // ========== CAMBIAR ROL DE USUARIO ==========

    @PatchMapping("/users/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(@PathVariable String username,
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

    // ========== BUSCAR USUARIO POR EMAIL ==========

    @GetMapping("/users/search/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchUserByEmail(@RequestParam String email) {
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

    // ========== MeTODO UTILITARIO ==========

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
}