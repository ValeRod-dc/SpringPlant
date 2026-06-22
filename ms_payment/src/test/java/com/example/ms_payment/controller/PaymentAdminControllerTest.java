package com.example.ms_payment.controller;

import com.example.ms_payment.dto.response.PaymentResponseDTO;
import com.example.ms_payment.model.PaymentMethod;
import com.example.ms_payment.model.PaymentStatus;
import com.example.ms_payment.security.jwt.JwtService;
import com.example.ms_payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserPayments() throws Exception {
        Long userId = 102L;
        List<PaymentResponseDTO> list = List.of(
                PaymentResponseDTO.builder()
                        .paymentId(1L)
                        .orderId(2L)
                        .userId(userId)
                        .amount(125.50)
                        .method(PaymentMethod.CREDIT_CARD)
                        .status(PaymentStatus.COMPLETED)
                        .build()
        );

        when(paymentService.getPaymentsByUser(anyLong())).thenReturn(list);

        mockMvc.perform(get("/api/v1/payments/admin/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(102))
                .andExpect(jsonPath("$[0]._links.self.href").exists())
                .andExpect(jsonPath("$[0]._links.details.href").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCheckPaymentExistsByOrder() throws Exception {
        Long orderId = 2L;
        when(paymentService.paymentExistsByOrder(anyLong())).thenReturn(true);

        mockMvc.perform(get("/api/v1/payments/admin/exists/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldReturn403WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/payments/admin/user/1"))
                .andExpect(status().isForbidden());
    }
}