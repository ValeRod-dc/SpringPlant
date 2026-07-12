package com.example.ms_payment.controller;

import com.example.ms_payment.dto.request.PaymentRequestDTO;
import com.example.ms_payment.dto.response.PaymentResponseDTO;
import com.example.ms_payment.model.PaymentMethod;
import com.example.ms_payment.model.PaymentStatus;
import com.example.ms_payment.security.jwt.JwtService;
import com.example.ms_payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "testUser", roles = "CLIENT")
    void shouldProcessPayment() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId(2L);
        request.setAmount(125.50);
        request.setMethod(PaymentMethod.CREDIT_CARD);

        PaymentResponseDTO response = PaymentResponseDTO.builder()
                .paymentId(1L)
                .orderId(2L)
                .userId(102L)
                .amount(125.50)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("txn_123")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.processPayment(any(String.class), any(PaymentRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.orderId").value(2))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.order-payments.href").exists());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenAmountMissing() throws Exception {
        String json = """
                {
                    "orderId": 2,
                    "method": "CREDIT_CARD"
                }
                """;

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenOrderIdMissing() throws Exception {
        String json = """
                {
                    "amount": 125.50,
                    "method": "CREDIT_CARD"
                }
                """;

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldGetPaymentById() throws Exception {
        Long paymentId = 1L;
        PaymentResponseDTO response = PaymentResponseDTO.builder()
                .paymentId(paymentId)
                .orderId(2L)
                .userId(102L)
                .amount(125.50)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.getPaymentById(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.orderId").value(2))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @WithMockUser
    void shouldGetPaymentsByOrder() throws Exception {
        Long orderId = 2L;
        List<PaymentResponseDTO> list = List.of(
                PaymentResponseDTO.builder()
                        .paymentId(1L)
                        .orderId(orderId)
                        .amount(125.50)
                        .status(PaymentStatus.COMPLETED)
                        .build()
        );

        when(paymentService.getPaymentsByOrder(anyLong())).thenReturn(list);

        mockMvc.perform(get("/api/v1/payments/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(2))
                .andExpect(jsonPath("$[0]._links.self.href").exists());
    }
}