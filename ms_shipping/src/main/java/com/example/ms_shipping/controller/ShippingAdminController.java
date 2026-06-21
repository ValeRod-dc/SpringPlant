package com.example.ms_shipping.controller;

import com.example.ms_shipping.client.UserClient;
import com.example.ms_shipping.dto.response.ShippingResponseDTO;
import com.example.ms_shipping.exception.custom.UserNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/shipping/admin")
@RequiredArgsConstructor
@Tag(name = "Administración de Envíos", description = "Endpoints exclusivos para administradores")
@SecurityRequirement(name = "bearerAuth")
public class ShippingAdminController {

    private final ShippingService shippingService;
    private final UserClient userClient;

    @Operation(
            summary = "Obtener todos los envíos de un usuario (por ID)",
            description = "Lista todos los envíos asociados a un userId específico. " +
                    "Acceso restringido a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de envíos del usuario",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado (si se valida)")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShippingResponseDTO>> getUserShippings(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long userId) {
        if(!userClient.userExistsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }
        log.info("ADMIN - Consultando envíos del usuario: {}", userId);
        return ResponseEntity.ok(shippingService.getByUser(userId));
    }

    @Operation(
            summary = "Listar envíos por estado",
            description = "Filtra todos los envíos según el estado proporcionado. Solo administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de envíos con ese estado",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class)))
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShippingResponseDTO>> getByStatus(
            @Parameter(description = "Estado del envío", example = "PENDING", required = true,
                    schema = @Schema(implementation = ShippingStatus.class))
            @PathVariable ShippingStatus status) {

        log.info("ADMIN - Consultando envíos por estado: {}", status);
        return ResponseEntity.ok(shippingService.getByStatus(status));
    }

    @Operation(
            summary = "Verificar si existe un envío para una orden",
            description = "Retorna `true` si ya existe un envío asociado a la orden dada, " +
                    "de lo contrario `false`. Solo administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado de la verificación",
                    content = @Content(schema = @Schema(type = "boolean")))
    })
    @GetMapping("/exists/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> shippingExistsByOrder(
            @Parameter(description = "ID de la orden", example = "1001", required = true)
            @PathVariable Long orderId) {

        log.debug("ADMIN - Verificando si existe envío para orden: {}", orderId);
        boolean exists = shippingService.shippingExistsByOrder(orderId);
        return ResponseEntity.ok(exists);
    }
}