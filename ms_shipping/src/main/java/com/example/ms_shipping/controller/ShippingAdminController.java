package com.example.ms_shipping.controller;

import com.example.ms_shipping.dto.response.ShippingResponseDTO;
import com.example.ms_shipping.model.ShippingStatus;
import com.example.ms_shipping.service.ShippingService;
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
public class ShippingAdminController {

    private final ShippingService shippingService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShippingResponseDTO>> getUserShippings(@PathVariable Long userId) {
        log.info("ADMIN - Consultando envíos del usuario: {}", userId);
        return ResponseEntity.ok(shippingService.getByUser(userId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShippingResponseDTO>> getByStatus(@PathVariable ShippingStatus status) {
        log.info("ADMIN - Consultando envíos por estado: {}", status);
        return ResponseEntity.ok(shippingService.getByStatus(status));
    }

    @GetMapping("/exists/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> shippingExistsByOrder(@PathVariable Long orderId) {
        log.debug("ADMIN - Verificando si existe envío para orden: {}", orderId);
        boolean exists = shippingService.shippingExistsByOrder(orderId);
        return ResponseEntity.ok(exists);
    }
}