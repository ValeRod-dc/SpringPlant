package com.example.ms_shipping.controller;

import com.example.ms_shipping.dto.request.CreateShippingRequest;
import com.example.ms_shipping.dto.response.ShippingResponseDTO;
import com.example.ms_shipping.model.ShippingStatus;
import com.example.ms_shipping.security.filter.JwtAuthFilter;
import com.example.ms_shipping.security.jwt.JwtService;
import com.example.ms_shipping.service.CustomUserDetailsService;
import com.example.ms_shipping.service.ShippingService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShippingController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "ADMIN")
class ShippingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShippingService shippingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(username = "testUser", roles = "ADMIN")
    void shouldCreateShipping() throws Exception {
        // Given
        CreateShippingRequest request = new CreateShippingRequest();
        request.setOrderId(2L);
        request.setAddress("Av. Siempre Viva 123");

        ShippingResponseDTO response = ShippingResponseDTO.builder()
                .shippingId(1L)
                .orderId(2L)
                .userId(102L)
                .address("Av. Siempre Viva 123")
                .status(ShippingStatus.PENDING)
                .trackingNumber("TRKABC123")
                .createdAt(LocalDateTime.now())
                .build();

        when(shippingService.createShipping(any(String.class), any(CreateShippingRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingId").value(1))
                .andExpect(jsonPath("$.orderId").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.trackingNumber").value("TRKABC123"))
                // Verificar enlaces HATEOAS
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.order-shippings.href").exists())
                .andExpect(jsonPath("$._links.my-shippings.href").exists())
                .andExpect(jsonPath("$._links.update-status.href").exists());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenOrderIdMissing() throws Exception {
        String json = """
                {
                    "address": "Av. Siempre Viva 123"
                }
                """;

        mockMvc.perform(post("/api/v1/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenAddressMissing() throws Exception {
        String json = """
                {
                    "orderId": 2
                }
                """;

        mockMvc.perform(post("/api/v1/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldGetShippingById() throws Exception {
        Long shippingId = 1L;
        ShippingResponseDTO response = ShippingResponseDTO.builder()
                .shippingId(shippingId)
                .orderId(2L)
                .userId(102L)
                .address("Av. Siempre Viva 123")
                .status(ShippingStatus.PENDING)
                .trackingNumber("TRKABC123")
                .createdAt(LocalDateTime.now())
                .build();

        when(shippingService.getShipping(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/shipping/{id}", shippingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingId").value(1))
                .andExpect(jsonPath("$.orderId").value(2))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @WithMockUser
    void shouldUpdateShippingStatus() throws Exception {
        Long shippingId = 1L;
        ShippingStatus newStatus = ShippingStatus.SHIPPED;
        ShippingResponseDTO response = ShippingResponseDTO.builder()
                .shippingId(shippingId)
                .orderId(2L)
                .userId(102L)
                .address("Av. Siempre Viva 123")
                .status(ShippingStatus.SHIPPED)
                .trackingNumber("TRKABC123")
                .shippedAt(LocalDateTime.now())
                .estimatedDelivery(LocalDateTime.now().plusDays(5))
                .createdAt(LocalDateTime.now())
                .build();

        when(shippingService.updateStatus(anyLong(), any(ShippingStatus.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/shipping/{id}/status", shippingId)
                        .param("status", newStatus.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.shippedAt").exists())
                .andExpect(jsonPath("$.estimatedDelivery").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }
}