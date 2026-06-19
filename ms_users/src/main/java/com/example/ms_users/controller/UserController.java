package com.example.ms_users.controller;

import com.example.ms_users.dto.request.UserUpdateDTO;
import com.example.ms_users.dto.response.UserResponseDTO;
import com.example.ms_users.exception.custom.UserNotFoundException;
import com.example.ms_users.model.User;
import com.example.ms_users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Perfil de usuario", description = "Endpoints para que los usuarios gestionen su propio perfil")
public class UserController {

    private final UserService userService;

    // ========== PERFIL PROPIO ==========

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Obtener mi perfil", description = "Retorna los datos del usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de obtener perfil - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.info("Obteniendo perfil - Usuario: {}", username);

        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));

            UserResponseDTO profile = UserResponseDTO.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .phone(user.getPhone())
                    .createdAt(user.getCreatedAt())
                    .build();

            log.debug("Perfil obtenido exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of("user", profile));

        } catch (UserNotFoundException e) {
            log.error("Usuario no encontrado - {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Actualizar mi perfil", description = "Modifica email, dirección o teléfono del usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "email ya en uso ???????????")
    })
    public ResponseEntity<?> updateMyProfile(Authentication authentication,
                                             @Valid @RequestBody UserUpdateDTO updateDTO) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de actualizar perfil - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.info("Actualizando perfil - Usuario: {}, Email: {}, Teléfono: {}",
                username, updateDTO.getEmail(), updateDTO.getPhone());

        try {
            User updatedUser = userService.updateUserProfile(username, updateDTO);

            UserResponseDTO response = UserResponseDTO.builder()
                    .userId(updatedUser.getUserId())
                    .username(updatedUser.getUsername())
                    .email(updatedUser.getEmail())
                    .role(updatedUser.getRole().name())
                    .phone(updatedUser.getPhone())
                    .createdAt(updatedUser.getCreatedAt())
                    .build();

            log.info("Perfil actualizado exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Perfil actualizado correctamente",
                    "user", response
            ));

        } catch (RuntimeException e) {
            log.error("Error actualizando perfil - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Eliminar mi cuenta", description = "Elimina definitivamente la cuenta del usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta eliminada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> deleteOwnAccount(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de eliminar cuenta - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.warn("Solicitud de eliminación de cuenta - Usuario: {}", username);

        try {
            userService.deleteByUsername(username);
            log.info("Cuenta eliminada exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Tu cuenta ha sido eliminada correctamente",
                    "username", username
            ));

        } catch (Exception e) {
            log.error("Error eliminando cuenta - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}