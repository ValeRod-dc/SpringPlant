package com.example.ms_payment.service;

import com.example.ms_payment.client.CartClient;
import com.example.ms_payment.client.OrderClient;
import com.example.ms_payment.client.UserClient;
import com.example.ms_payment.dto.OrderDTO;
import com.example.ms_payment.dto.request.PaymentRequestDTO;
import com.example.ms_payment.dto.response.PaymentResponseDTO;
import com.example.ms_payment.exception.custom.*;
import com.example.ms_payment.model.Payment;
import com.example.ms_payment.model.PaymentMethod;
import com.example.ms_payment.model.PaymentStatus;
import com.example.ms_payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderClient orderClient;

    @Mock
    private CartClient cartClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequestDTO validRequest;
    private OrderDTO validOrder;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        validRequest = new PaymentRequestDTO();
        validRequest.setOrderId(2L);
        validRequest.setAmount(125.50);
        validRequest.setMethod(PaymentMethod.CREDIT_CARD);

        validOrder = new OrderDTO();
        validOrder.setId(2L);
        validOrder.setClientId(102L);
        validOrder.setTotalAmount(125.50);
        validOrder.setPaymentStatus("PENDING");

        savedPayment = Payment.builder()
                .paymentId(1L)
                .orderId(2L)
                .userId(102L)
                .amount(125.50)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("txn_123")
                .build();
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        String username = "testUser";
        when(userClient.userExists(username)).thenReturn(true);
        when(orderClient.getOrderById(2L)).thenReturn(validOrder);
        when(paymentRepository.existsByOrderId(2L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        doNothing().when(orderClient).updateOrderPaymentStatus(2L, "PAID");
        doNothing().when(cartClient).clearCart(102L);

        PaymentResponseDTO result = paymentService.processPayment(username, validRequest);

        assertNotNull(result);
        assertEquals(1L, result.getPaymentId());
        assertEquals(2L, result.getOrderId());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());

        verify(userClient).userExists(username);
        verify(orderClient).getOrderById(2L);
        verify(paymentRepository).save(any(Payment.class));
        verify(orderClient).updateOrderPaymentStatus(2L, "PAID");
        verify(cartClient).clearCart(102L);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        String username = "unknown";
        when(userClient.userExists(username)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () ->
                paymentService.processPayment(username, validRequest)
        );

        verify(orderClient, never()).getOrderById(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        String username = "testUser";
        when(userClient.userExists(username)).thenReturn(true);
        when(orderClient.getOrderById(2L)).thenThrow(new RuntimeException("Order not found"));

        assertThrows(OrderNotFoundException.class, () ->
                paymentService.processPayment(username, validRequest)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldThrowOrderAlreadyPaidExceptionWhenPaymentExists() {
        String username = "testUser";
        when(userClient.userExists(username)).thenReturn(true);
        when(orderClient.getOrderById(2L)).thenReturn(validOrder);
        when(paymentRepository.existsByOrderId(2L)).thenReturn(true);

        assertThrows(OrderAlreadyPaidException.class, () ->
                paymentService.processPayment(username, validRequest)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldThrowPaymentAmountMismatchExceptionWhenAmountDoesNotMatch() {
        String username = "testUser";
        validRequest.setAmount(200.0); // Diferente
        when(userClient.userExists(username)).thenReturn(true);
        when(orderClient.getOrderById(2L)).thenReturn(validOrder);
        when(paymentRepository.existsByOrderId(2L)).thenReturn(false);

        assertThrows(PaymentAmountMismatchException.class, () ->
                paymentService.processPayment(username, validRequest)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldReturnPaymentById() {
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(savedPayment));

        PaymentResponseDTO result = paymentService.getPaymentById(paymentId);
        assertNotNull(result);
        assertEquals(1L, result.getPaymentId());
        assertEquals(2L, result.getOrderId());

        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void shouldThrowPaymentNotFoundExceptionWhenPaymentNotFound() {
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () ->
                paymentService.getPaymentById(paymentId)
        );

        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void shouldReturnPaymentsByUser() {
        Long userId = 102L;
        when(paymentRepository.findByUserId(userId)).thenReturn(java.util.List.of(savedPayment));

        var result = paymentService.getPaymentsByUser(userId);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(paymentRepository).findByUserId(userId);
    }

    @Test
    void shouldReturnPaymentsByOrder() {
        Long orderId = 2L;
        when(paymentRepository.findByOrderId(orderId)).thenReturn(java.util.List.of(savedPayment));

        var result = paymentService.getPaymentsByOrder(orderId);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    void shouldCheckPaymentExistsByOrder() {
        Long orderId = 2L;
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(true);

        assertTrue(paymentService.paymentExistsByOrder(orderId));
        verify(paymentRepository).existsByOrderId(orderId);
    }
}