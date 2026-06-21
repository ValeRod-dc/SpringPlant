package com.example.ms_shipping.controller;

import com.example.ms_shipping.dto.request.CreateShippingRequest;
import com.example.ms_shipping.dto.response.ShippingResponseDTO;
import com.example.ms_shipping.model.ShippingStatus;
import com.example.ms_shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@Tag(name = "Envíos", description = "Endpoints para la gestión de envíos (clientes, empleados y administradores)")
@SecurityRequirement(name = "bearerAuth")
public class ShippingController {

    private final ShippingService shippingService;

    @Operation(
            summary = "Crear un nuevo envío",
            description = "Permite crear un envío asociado a una orden que debe estar en estado 'PAID'. " +
                    "El usuario autenticado debe existir en el sistema. Solo se permite un envío por orden."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envío creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o la orden no está pagada"),
            @ApiResponse(responseCode = "404", description = "Usuario u orden no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe un envío para esta orden")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<ShippingResponseDTO> createShipping(
            Authentication authentication,
            @Valid @RequestBody CreateShippingRequest request) {
        String username = authentication.getName();
        log.info("Solicitud de creación de envío - Usuario: {}, Orden: {}", username, request.getOrderId());
        ShippingResponseDTO created = shippingService.createShipping(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Obtener un envío por su ID",
            description = "Devuelve los detalles de un envío específico. Requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envío encontrado",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<ShippingResponseDTO> getById(
            @Parameter(description = "ID del envío", example = "1", required = true)
            @PathVariable Long id) {

        log.info("Consultando envío con id: {}", id);
        return ResponseEntity.ok(shippingService.getShipping(id));
    }

    @Operation(
            summary = "Listar envíos por ID de orden",
            description = "Retorna todos los envíos asociados a una orden específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de envíos (puede estar vacía)",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada (si el servicio de órdenes responde)")
    })
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<ShippingResponseDTO>> getByOrder(
            @Parameter(description = "ID de la orden", example = "2", required = true)
            @PathVariable Long orderId) {

        log.info("Consultando envíos por orderId: {}", orderId);
        return ResponseEntity.ok(shippingService.getByOrder(orderId));
    }

    @Operation(
            summary = "Obtener mis envíos (usuario autenticado)",
            description = "Devuelve todos los envíos del usuario actualmente autenticado. " +
                    "Actualmente usa un userId fijo (1) hasta que se integre con el servicio de usuarios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de envíos del usuario",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class)))
    })
    @GetMapping("/my-shippings")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<ShippingResponseDTO>> getMyShippings(Authentication authentication) {
        String username = authentication.getName();
        log.info("Consultando envíos del usuario: {}", username);
        // TODO: Obtener userId real desde ms_users
        Long userId = 1L;
        return ResponseEntity.ok(shippingService.getByUser(userId));
    }

    @Operation(
            summary = "Actualizar el estado de un envío",
            description = "Permite a administradores y empleados cambiar el estado de un envío. " +
                    "Cuando se cambia a 'SHIPPED' se asigna fecha de envío y estimación; " +
                    "cuando a 'DELIVERED' se registra la fecha de entrega."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido o transición no permitida"),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ShippingResponseDTO> updateStatus(
            @Parameter(description = "ID del envío", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado del envío", example = "SHIPPED", required = true,
                    schema = @Schema(implementation = ShippingStatus.class))
            @RequestParam ShippingStatus status) {

        log.info("Actualizando estado del envío {} a {}", id, status);
        ShippingResponseDTO updated = shippingService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}