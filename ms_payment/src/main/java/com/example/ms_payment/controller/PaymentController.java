package com.example.ms_payment.controller;

import com.example.ms_payment.dto.request.PaymentRequestDTO;
import com.example.ms_payment.dto.response.PaymentResponseDTO;
import com.example.ms_payment.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<PaymentResponseDTO> processPayment(Authentication authentication,
                                                             @Valid @RequestBody PaymentRequestDTO request) {
        String username = authentication.getName();
        log.info("Solicitud de pago - Usuario: {}, OrderId: {}", username, request.getOrderId());
        PaymentResponseDTO response = paymentService.processPayment(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long paymentId) {
        log.info("Consultando pago con id: {}", paymentId);
        PaymentResponseDTO response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByOrder(@PathVariable Long orderId) {
        log.info("Consultando pagos para orden: {}", orderId);
        List<PaymentResponseDTO> response = paymentService.getPaymentsByOrder(orderId);
        return ResponseEntity.ok(response);
    }
}