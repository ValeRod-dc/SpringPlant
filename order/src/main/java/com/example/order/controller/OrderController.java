package com.example.order.controller;

import com.example.order.model.Order;
import com.example.order.model.enums.OrderStatus;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Gestión de órdenes de compra")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;


    @Operation(
            summary = "Crear orden",
            description = "Registra una nueva orden en el sistema. Accesible para ADMIN, EMPLOYEE y CLIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        log.info("Creando nueva orden");
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
    }

    @Operation(
            summary = "Obtener todas las órdenes",
            description = "Retorna la lista completa de órdenes. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<Order>> getAllOrders() {
        log.info("Obteniendo todas las órdenes");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(
            summary = "Obtener órdenes por estado",
            description = "Retorna todas las órdenes que coinciden con el estado indicado. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @Parameter(description = "Estado de la orden", example = "PENDING")
            @PathVariable OrderStatus status) {
        log.info("Obteniendo órdenes por estado: {}", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @Operation(
            summary = "Obtener órdenes por cliente",
            description = "Retorna todas las órdenes asociadas a un cliente específico. Accesible para ADMIN, EMPLOYEE y CLIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<Order>> getOrdersByClient(
            @Parameter(description = "ID del cliente", example = "5")
            @PathVariable Long clientId) {
        log.info("Obteniendo órdenes del cliente: {}", clientId);
        return ResponseEntity.ok(orderService.getOrdersByClientId(clientId));
    }

    @Operation(
            summary = "Obtener orden por ID",
            description = "Busca y retorna una orden según su identificador único. Accesible para ADMIN, EMPLOYEE y CLIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "ID de la orden", example = "1")
            @PathVariable Long id) {
        log.info("Obteniendo orden por ID: {}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(
            summary = "Verificar existencia de orden",
            description = "Verifica si una orden existe por su ID. Endpoint de uso interno entre microservicios, no requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/exists/{id}")
    public ResponseEntity<Order> orderExists(
            @Parameter(description = "ID de la orden a verificar", example = "1")
            @PathVariable Long id) {
        log.debug("Verificando existencia de orden con ID: {}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(
            summary = "Actualizar orden",
            description = "Modifica los datos completos de una orden existente. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Order> updateOrder(
            @Parameter(description = "ID de la orden a actualizar", example = "1")
            @PathVariable Long id,
            @RequestBody Order orderData) {
        log.info("Actualizando orden ID: {}", id);
        return ResponseEntity.ok(orderService.updateOrder(id, orderData));
    }

    @Operation(
            summary = "Cambiar estado de la orden",
            description = "Actualiza únicamente el estado de una orden. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Order> changeOrderStatus(
            @Parameter(description = "ID de la orden", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la orden", example = "SHIPPED")
            @RequestParam OrderStatus status) {
        log.info("Cambiando estado de orden ID: {} a {}", id, status);
        return ResponseEntity.ok(orderService.changeOrderStatus(id, status));
    }

    @Operation(
            summary = "Actualizar estado de pago",
            description = "Actualiza el estado de pago de una orden. Endpoint de uso interno entre microservicios, no requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de pago actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de pago inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<Order> updatePaymentStatus(
            @Parameter(description = "ID de la orden", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado de pago", example = "PAID")
            @RequestParam String status) {
        log.info("Actualizando estado de pago de orden ID: {} a {}", id, status);
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, status));
    }

    @Operation(
            summary = "Eliminar orden",
            description = "Elimina una orden del sistema por su ID. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<String> deleteOrder(
            @Parameter(description = "ID de la orden a eliminar", example = "1")
            @PathVariable Long id) {
        log.info("Eliminando orden ID: {}", id);
        return ResponseEntity.ok(orderService.deleteOrder(id));
    }
}