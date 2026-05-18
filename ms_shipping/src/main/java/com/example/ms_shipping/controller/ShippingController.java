package com.example.ms_shipping.controller;

import com.example.ms_shipping.dto.request.CreateShippingRequest;
import com.example.ms_shipping.dto.response.ShippingResponseDTO;
import com.example.ms_shipping.model.ShippingStatus;
import com.example.ms_shipping.service.ShippingService;
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
public class ShippingController {

    private final ShippingService shippingService;

    // Crear envío (solo para órdenes pagadas)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<ShippingResponseDTO> createShipping(Authentication authentication,
                                                              @Valid @RequestBody CreateShippingRequest request) {
        String username = authentication.getName();
        log.info("Solicitud de creación de envío - Usuario: {}, Orden: {}", username, request.getOrderId());
        ShippingResponseDTO created = shippingService.createShipping(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Obtener envío por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<ShippingResponseDTO> getById(@PathVariable Long id) {
        log.info("Consultando envío con id: {}", id);
        return ResponseEntity.ok(shippingService.getShipping(id));
    }

    // Obtener envíos por orden
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<ShippingResponseDTO>> getByOrder(@PathVariable Long orderId) {
        log.info("Consultando envíos por orderId: {}", orderId);
        return ResponseEntity.ok(shippingService.getByOrder(orderId));
    }

    // Obtener mis envíos (por usuario autenticado)
    @GetMapping("/my-shippings")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<ShippingResponseDTO>> getMyShippings(Authentication authentication) {
        String username = authentication.getName();
        log.info("Consultando envíos del usuario: {}", username);
        // TODO: Obtener userId real desde ms_users
        Long userId = 1L;
        return ResponseEntity.ok(shippingService.getByUser(userId));
    }

    // Actualizar estado del envío (solo ADMIN o EMPLOYEE)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ShippingResponseDTO> updateStatus(@PathVariable Long id,
                                                            @RequestParam ShippingStatus status) {
        log.info("Actualizando estado del envío {} a {}", id, status);
        ShippingResponseDTO updated = shippingService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}