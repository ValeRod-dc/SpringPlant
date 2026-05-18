package com.example.order.controller;

import com.example.order.model.Order;
import com.example.order.model.enums.OrderStatus;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        log.info("Creando nueva orden");
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<Order>> getAllOrders() {
        log.info("Obteniendo todas las órdenes");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("Obteniendo órdenes por estado: {}", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<Order>> getOrdersByClient(@PathVariable Long clientId) {
        log.info("Obteniendo órdenes del cliente: {}", clientId);
        return ResponseEntity.ok(orderService.getOrdersByClientId(clientId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.info("Obteniendo orden por ID: {}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Order> orderExists(@PathVariable Long id) {
        log.debug("Verificando existencia de orden con ID: {}", id);
        Order order = orderService.getOrderById(id); // ya tienes este método
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderData) {
        log.info("Actualizando orden ID: {}", id);
        return ResponseEntity.ok(orderService.updateOrder(id, orderData));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Order> changeOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        log.info("Cambiando estado de orden ID: {} a {}", id, status);
        return ResponseEntity.ok(orderService.changeOrderStatus(id, status));
    }

    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<Order> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("Actualizando estado de pago de orden ID: {} a {}", id, status);
        Order updatedOrder = orderService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        log.info("Eliminando orden ID: {}", id);
        return ResponseEntity.ok(orderService.deleteOrder(id));
    }
}