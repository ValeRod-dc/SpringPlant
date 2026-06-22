package com.example.ms_discount.controller;

import com.example.ms_discount.dto.request.CreateCouponRequest;
import com.example.ms_discount.dto.response.DiscountResponseDTO;
import com.example.ms_discount.model.DiscountType;
import com.example.ms_discount.security.jwt.JwtService;
import com.example.ms_discount.service.DiscountService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscountControllerAdmin.class)
@AutoConfigureMockMvc(addFilters = false)
class DiscountControllerAdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DiscountService discountService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCoupon() throws Exception {
        // Given
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode("ADMIN10");
        request.setDescription("Admin coupon");
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(10.0);
        request.setValidFrom(LocalDateTime.now().minusDays(1));
        request.setValidUntil(LocalDateTime.now().plusDays(30));
        request.setMaxUses(10);
        request.setMinPurchaseAmount(0.0);
        request.setActive(true);

        DiscountResponseDTO response = DiscountResponseDTO.builder()
                .discountId(1L)
                .code("ADMIN10")
                .description("Admin coupon")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(10.0)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .maxUses(10)
                .currentUses(0)
                .minPurchaseAmount(0.0)
                .active(true)
                .build();

        when(discountService.createCoupon(any(CreateCouponRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/discounts/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ADMIN10"))
                .andExpect(jsonPath("$.discountValue").value(10.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeactivateCoupon() throws Exception {
        // Given
        Long couponId = 1L;
        doNothing().when(discountService).deactivateCoupon(couponId);

        // When & Then
        mockMvc.perform(delete("/api/v1/discounts/admin/{id}", couponId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldActivateCoupon() throws Exception {
        // Given
        Long couponId = 1L;
        DiscountResponseDTO response = DiscountResponseDTO.builder()
                .discountId(couponId)
                .code("ADMIN10")
                .active(true)
                .build();

        doNothing().when(discountService).activateCoupon(couponId);
        when(discountService.getCouponByCode(any())).thenReturn(response);
        when(discountService.findByIdOrThrow(couponId)).thenReturn(null);

        // When & Then
        mockMvc.perform(patch("/api/v1/discounts/admin/{id}/activate", couponId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }
}