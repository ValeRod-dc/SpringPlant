package com.example.notification.controller;

import com.example.notification.dto.NotificationRequestDTO;
import com.example.notification.dto.NotificationResponseDTO;
import com.example.notification.model.enums.NotificationStatus;
import com.example.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones enviadas a los usuarios")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;


    @Operation(
            summary = "Obtener todas las notificaciones",
            description = "Retorna la lista completa de notificaciones. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<NotificationResponseDTO>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }


    @Operation(
            summary = "Obtener notificación por ID",
            description = "Busca y retorna una notificación según su identificador único. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación encontrada"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<NotificationResponseDTO> getById(
            @Parameter(description = "ID de la notificación a buscar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @Operation(
            summary = "Obtener notificaciones por usuario",
            description = "Retorna todas las notificaciones asociadas a un usuario específico. Accesible para ADMIN, EMPLOYEE y CLIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<NotificationResponseDTO>> getByUserId(
            @Parameter(description = "ID del usuario a consultar", example = "5")
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getByUserId(userId));
    }

    @Operation(
            summary = "Obtener notificaciones por estado",
            description = "Retorna todas las notificaciones que coinciden con el estado indicado. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<NotificationResponseDTO>> getByStatus(
            @Parameter(description = "Estado de la notificación", example = "PENDING")
            @PathVariable NotificationStatus status) {
        return ResponseEntity.ok(notificationService.getByStatus(status));
    }

    @Operation(
            summary = "Crear notificación",
            description = "Registra y envía una nueva notificación a un usuario. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notificación creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<NotificationResponseDTO> create(
            @Valid @RequestBody NotificationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.save(dto));
    }

    @Operation(
            summary = "Marcar notificación como enviada",
            description = "Actualiza el estado de una notificación a SENT. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación marcada como enviada"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PatchMapping("/{id}/sent")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<NotificationResponseDTO> markAsSent(
            @Parameter(description = "ID de la notificación a marcar como enviada", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsSent(id));
    }

    @Operation(
            summary = "Marcar notificación como fallida",
            description = "Actualiza el estado de una notificación a FAILED. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación marcada como fallida"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PatchMapping("/{id}/failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<NotificationResponseDTO> markAsFailed(
            @Parameter(description = "ID de la notificación a marcar como fallida", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsFailed(id));
    }

    @Operation(
            summary = "Eliminar notificación",
            description = "Elimina una notificación del sistema por su ID. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(
            @Parameter(description = "ID de la notificación a eliminar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.delete(id));
    }
}