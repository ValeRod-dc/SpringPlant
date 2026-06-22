package com.example.ms_discount.controller;

import com.example.ms_discount.dto.response.DiscountResponseDTO;
import com.example.ms_discount.model.DiscountType;
import com.example.ms_discount.security.jwt.JwtService;
import com.example.ms_discount.service.DiscountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscountControllerEmployee.class)
@AutoConfigureMockMvc(addFilters = false)
class DiscountControllerEmployeeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiscountService discountService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldListAllCoupons() throws Exception {
        // Given
        List<DiscountResponseDTO> list = List.of(
                DiscountResponseDTO.builder()
                        .discountId(1L)
                        .code("TEST10")
                        .description("Test coupon")
                        .discountType(DiscountType.PERCENTAGE)
                        .discountValue(10.0)
                        .active(true)
                        .build()
        );

        when(discountService.listAll()).thenReturn(list);

        // When & Then
        mockMvc.perform(get("/api/v1/discounts/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("TEST10"))
                .andExpect(jsonPath("$[0].discountValue").value(10.0));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldListActiveCoupons() throws Exception {
        // Given
        List<DiscountResponseDTO> list = List.of(
                DiscountResponseDTO.builder()
                        .discountId(1L)
                        .code("ACTIVE10")
                        .active(true)
                        .build()
        );

        when(discountService.listActiveCoupons()).thenReturn(list);

        // When & Then
        mockMvc.perform(get("/api/v1/discounts/employee/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("ACTIVE10"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldGetCouponByCode() throws Exception {
        // Given
        String code = "TEST10";
        DiscountResponseDTO response = DiscountResponseDTO.builder()
                .discountId(1L)
                .code(code)
                .description("Test coupon")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(10.0)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        when(discountService.getCouponByCode(anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/discounts/employee/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldCheckCouponExistsByCode() throws Exception {
        // Given
        String code = "TEST10";
        when(discountService.couponExists(code)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/discounts/employee/exists/code/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }
}