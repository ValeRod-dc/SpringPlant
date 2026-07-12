package com.example.ms_payment.controller;

import com.example.ms_payment.dto.request.PaymentRequestDTO;
import com.example.ms_payment.dto.response.PaymentResponseDTO;
import com.example.ms_payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Endpoints para procesar y consultar pagos")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Procesar pago", description = "Procesa el pago de una orden para el usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago procesado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o monto no coincide"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Orden o carrito no encontrado"),
            @ApiResponse(responseCode = "409", description = "La orden ya fue pagada")
    })
    public ResponseEntity<PaymentResponseDTO> processPayment(Authentication authentication,
                                                             @Valid @RequestBody PaymentRequestDTO request) {
        String username = authentication.getName();
        log.info("Solicitud de pago - Usuario: {}, OrderId: {}", username, request.getOrderId());
        PaymentResponseDTO response = paymentService.processPayment(username, request);
        addPaymentLinks(response, response.getPaymentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Obtener pago por ID", description = "Retorna el detalle de un pago a partir de su identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long paymentId) {
        log.info("Consultando pago con id: {}", paymentId);
        PaymentResponseDTO response = paymentService.getPaymentById(paymentId);
        addPaymentLinks(response, paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Obtener pagos por orden", description = "Retorna todos los pagos asociados a una orden.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de pagos de la orden"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByOrder(@PathVariable Long orderId) {
        log.info("Consultando pagos para orden: {}", orderId);
        List<PaymentResponseDTO> response = paymentService.getPaymentsByOrder(orderId);
        response.forEach(dto -> addPaymentLinks(dto, dto.getPaymentId()));
        return ResponseEntity.ok(response);
    }

    private void addPaymentLinks(PaymentResponseDTO dto, Long paymentId) {
        if (dto != null) {
            dto.add(linkTo(methodOn(PaymentController.class).getPaymentById(paymentId)).withSelfRel());
            dto.add(linkTo(methodOn(PaymentController.class).getPaymentsByOrder(dto.getOrderId())).withRel("order-payments"));
            dto.add(linkTo(methodOn(PaymentAdminController.class).getUserPayments(dto.getUserId())).withRel("user-payments"));
            dto.add(linkTo(methodOn(PaymentAdminController.class).paymentExistsByOrder(dto.getOrderId())).withRel("exists"));
        }
    }
}