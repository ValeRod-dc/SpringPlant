package com.example.ms_payment.controller;

import com.example.ms_payment.dto.response.PaymentResponseDTO;
import com.example.ms_payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/admin")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentService paymentService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponseDTO>> getUserPayments(@PathVariable Long userId) {
        log.info("ADMIN - Consultando pagos del usuario: {}", userId);
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/exists/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> paymentExistsByOrder(@PathVariable Long orderId) {
        log.debug("ADMIN - Verificando si existe pago para orden: {}", orderId);
        boolean exists = paymentService.paymentExistsByOrder(orderId);
        return ResponseEntity.ok(exists);
    }
}