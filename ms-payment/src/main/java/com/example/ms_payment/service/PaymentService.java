package com.example.ms_payment.service;

import com.example.ms_payment.client.CartClient;
import com.example.ms_payment.client.OrderClient;
import com.example.ms_payment.client.UserClient;
import com.example.ms_payment.dto.OrderDTO;
import com.example.ms_payment.dto.request.PaymentRequestDTO;
import com.example.ms_payment.dto.response.PaymentResponseDTO;
import com.example.ms_payment.exception.custom.*;
import com.example.ms_payment.model.Payment;
import com.example.ms_payment.model.PaymentStatus;
import com.example.ms_payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final CartClient cartClient;
    private final UserClient userClient;

    public Payment findByIdOrThrow(Long paymentId) {
        log.debug("Buscando pago por ID: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("Pago no encontrado - ID: {}", paymentId);
                    return new PaymentNotFoundException("Pago no encontrado con ID: " + paymentId);
                });
    }

    public List<PaymentResponseDTO> getPaymentsByUser(Long userId) {
        log.debug("Obteniendo pagos del usuario: {}", userId);
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentResponseDTO> getPaymentsByOrder(Long orderId) {
        log.debug("Obteniendo pagos por orden: {}", orderId);
        return paymentRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public boolean paymentExistsByOrder(Long orderId) {
        log.debug("Verificando si existe pago para orden: {}", orderId);
        return paymentRepository.existsByOrderId(orderId);
    }

    @Transactional
    public PaymentResponseDTO processPayment(String username, PaymentRequestDTO request) {
        log.info("Procesando pago - Usuario: {}, OrderId: {}, Monto: ${}",
                username, request.getOrderId(), request.getAmount());

        // 1. Validar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // 2. Obtener la orden de compra
        OrderDTO order;
        try {
            order = orderClient.getOrderById(request.getOrderId());
        } catch (Exception e) {
            log.error("Error al obtener la orden: {}", e.getMessage());
            throw new OrderNotFoundException("Orden no encontrada con ID: " + request.getOrderId());
        }

        // 3. Validar que la orden no esté ya pagada
        if (paymentExistsByOrder(request.getOrderId())) {
            log.warn("Ya existe un pago para la orden: {}", request.getOrderId());
            throw new OrderAlreadyPaidException("Ya existe un pago procesado para esta orden");
        }

        // 4. Validar que el monto coincida con el total de la orden
        if (!order.getTotalAmount().equals(request.getAmount())) {
            log.warn("Monto no coincide - Orden total: ${}, Pago: ${}", order.getTotalAmount(), request.getAmount());
            throw new PaymentAmountMismatchException("El monto del pago no coincide con el total de la orden. " +
                    "Total de la orden: $" + order.getTotalAmount());
        }

        // 5. Simular pasarela de pago
        String transactionId = UUID.randomUUID().toString();
        PaymentStatus status;
        String errorMessage = null;

        try {
            // Simulación de procesamiento de pago (siempre exitoso por ahora)
            status = PaymentStatus.COMPLETED;
            log.debug("Pago simulado exitoso, transactionId: {}", transactionId);
        } catch (Exception e) {
            status = PaymentStatus.FAILED;
            errorMessage = "Error en la pasarela de pago: " + e.getMessage();
            log.error(errorMessage);
            throw new PaymentProcessingException(errorMessage);
        }

        // 6. Guardar el pago
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(order.getClientId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(status)
                .transactionId(transactionId)
                .completedAt(status == PaymentStatus.COMPLETED ? LocalDateTime.now() : null)
                .errorMessage(errorMessage)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Pago guardado con id: {}, estado: {}", saved.getPaymentId(), saved.getStatus());

        // 7. Si el pago fue exitoso, actualizar la orden y limpiar el carrito
        if (status == PaymentStatus.COMPLETED) {
            try {
                orderClient.updateOrderPaymentStatus(request.getOrderId(), "PAID");
                log.info("Estado de orden actualizado a PAID para orden: {}", request.getOrderId());
            } catch (Exception e) {
                log.error("Error al actualizar estado de la orden: {}", e.getMessage());
                // No lanzamos excepción porque el pago ya fue procesado
            }

            try {
                cartClient.clearCart(order.getClientId());
                log.info("Carrito limpiado para usuario: {}", username);
            } catch (Exception e) {
                log.error("Error al limpiar el carrito: {}", e.getMessage());
                // No lanzamos excepción porque el pago ya fue procesado
            }
        }

        return mapToResponseDTO(saved);
    }

    public PaymentResponseDTO getPaymentById(Long paymentId) {
        log.debug("Obteniendo pago por id: {}", paymentId);
        Payment payment = findByIdOrThrow(paymentId);
        return mapToResponseDTO(payment);
    }

    private PaymentResponseDTO mapToResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .errorMessage(payment.getErrorMessage())
                .build();
    }
}