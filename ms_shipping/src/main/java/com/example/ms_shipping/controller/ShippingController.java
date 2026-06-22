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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@Tag(name = "Envíos", description = "Endpoints para la gestión de envíos (clientes, empleados y administradores)")
@SecurityRequirement(name = "bearerAuth")
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Crear un nuevo envío")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envío creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o la orden no está pagada"),
            @ApiResponse(responseCode = "404", description = "Usuario u orden no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe un envío para esta orden")
    })
    public ResponseEntity<?> createShipping(@Valid @RequestBody CreateShippingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Solicitud de creación de envío - Usuario: {}, Orden: {}", username, request.getOrderId());
        ShippingResponseDTO created = shippingService.createShipping(username, request);

        // Agregar enlaces HATEOAS
        created.add(linkTo(methodOn(ShippingController.class).getById(created.getShippingId())).withSelfRel());
        created.add(linkTo(methodOn(ShippingController.class).getByOrder(created.getOrderId())).withRel("order-shippings"));
        created.add(linkTo(methodOn(ShippingController.class).getMyShippings(authentication)).withRel("my-shippings"));
        created.add(linkTo(methodOn(ShippingController.class).updateStatus(created.getShippingId(), null)).withRel("update-status"));

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Obtener un envío por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envío encontrado",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    public ResponseEntity<ShippingResponseDTO> getById(
            @Parameter(description = "ID del envío", example = "1", required = true)
            @PathVariable Long id) {

        log.info("Consultando envío con id: {}", id);
        ShippingResponseDTO shipping = shippingService.getShipping(id);

        // Agregar enlaces HATEOAS
        shipping.add(linkTo(methodOn(ShippingController.class).getById(id)).withSelfRel());
        shipping.add(linkTo(methodOn(ShippingController.class).getMyShippings(null)).withRel("mis-envios"));
        shipping.add(linkTo(methodOn(ShippingController.class).updateStatus(id, null)).withRel("update-status"));

        // Enlace a la orden asociada (si tienes un endpoint en ms-orders)
        // shipping.add(linkTo(OrderController.class).slash(shipping.getOrderId()).withRel("orden"));

        return ResponseEntity.ok(shipping);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Listar envíos por ID de orden")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de envíos (puede estar vacía)",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class)))
    })
    public ResponseEntity<List<ShippingResponseDTO>> getByOrder(
            @Parameter(description = "ID de la orden", example = "1001", required = true)
            @PathVariable Long orderId) {

        log.info("Consultando envíos por orderId: {}", orderId);
        List<ShippingResponseDTO> shippings = shippingService.getByOrder(orderId);

        // Agregar enlaces a cada elemento
        shippings.forEach(shipping -> {
            shipping.add(linkTo(methodOn(ShippingController.class).getById(shipping.getShippingId())).withSelfRel());
            shipping.add(linkTo(methodOn(ShippingController.class).getMyShippings(null)).withRel("mis-envios"));
        });

        return ResponseEntity.ok(shippings);
    }

    @GetMapping("/my-shippings")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Obtener mis envíos (usuario autenticado)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de envíos del usuario",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class)))
    })
    public ResponseEntity<List<ShippingResponseDTO>> getMyShippings(Authentication authentication) {

        String username = authentication != null ? authentication.getName() : "anonimo";
        log.info("Consultando envíos del usuario: {}", username);
        // TODO: Obtener userId real desde ms_users
        Long userId = 1L;
        List<ShippingResponseDTO> shippings = shippingService.getByUser(userId);

        // Agregar enlaces
        shippings.forEach(shipping -> {
            shipping.add(linkTo(methodOn(ShippingController.class).getById(shipping.getShippingId())).withSelfRel());
            shipping.add(linkTo(methodOn(ShippingController.class).updateStatus(shipping.getShippingId(), null)).withRel("update-status"));
        });

        return ResponseEntity.ok(shippings);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Actualizar el estado de un envío")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = ShippingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido o transición no permitida"),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    public ResponseEntity<ShippingResponseDTO> updateStatus(
            @Parameter(description = "ID del envío", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado del envío", example = "SHIPPED", required = true,
                    schema = @Schema(implementation = ShippingStatus.class))
            @RequestParam ShippingStatus status) {

        log.info("Actualizando estado del envío {} a {}", id, status);
        ShippingResponseDTO updated = shippingService.updateStatus(id, status);

        // Agregar enlaces
        updated.add(linkTo(methodOn(ShippingController.class).getById(id)).withSelfRel());
        updated.add(linkTo(methodOn(ShippingController.class).getMyShippings(null)).withRel("mis-envios"));

        return ResponseEntity.ok(updated);
    }
}