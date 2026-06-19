package com.example.ms_payment.controller;

import com.example.ms_payment.dto.response.PaymentResponseDTO;
import com.example.ms_payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Pagos (Admin)", description = "Endpoints administrativos para consultar pagos")
public class PaymentAdminController {

    private final PaymentService paymentService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener pagos de un usuario", description = "Retorna todos los pagos realizados por un usuario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de pagos del usuario"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<PaymentResponseDTO>> getUserPayments(@PathVariable Long userId) {
        log.info("ADMIN - Consultando pagos del usuario: {}", userId);
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/exists/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar existencia de pago por orden", description = "Indica si existe un pago registrado para la orden dada. Uso interno entre microservicios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado de la verificación"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Boolean> paymentExistsByOrder(@PathVariable Long orderId) {
        log.debug("ADMIN - Verificando si existe pago para orden: {}", orderId);
        boolean exists = paymentService.paymentExistsByOrder(orderId);
        return ResponseEntity.ok(exists);
    }
}