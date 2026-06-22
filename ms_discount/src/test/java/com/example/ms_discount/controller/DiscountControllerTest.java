package com.example.ms_discount.controller;

import com.example.ms_discount.dto.response.DiscountResult;
import com.example.ms_discount.security.jwt.JwtService;
import com.example.ms_discount.service.DiscountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiscountController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiscountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiscountService discountService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "testUser", roles = "CLIENT")
    void shouldUseCouponWhenValid() throws Exception {
        // Given
        String code = "TEST10";
        Double cartTotal = 150.0;
        DiscountResult result = new DiscountResult(true, 15.0, "Cupón aplicado correctamente", code);

        when(discountService.useCoupon(anyString(), anyDouble())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/discounts/use")
                        .param("code", code)
                        .param("cartTotal", String.valueOf(cartTotal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(15.0))
                .andExpect(jsonPath("$.message").value("Cupón aplicado correctamente"))
                .andExpect(jsonPath("$.couponCode").value(code));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenCouponInvalid() throws Exception {
        // Given
        String code = "INVALID";
        Double cartTotal = 150.0;
        DiscountResult result = new DiscountResult(false, 0.0, "Cupón no válido", code);

        when(discountService.useCoupon(anyString(), anyDouble())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/discounts/use")
                        .param("code", code)
                        .param("cartTotal", String.valueOf(cartTotal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Cupón no válido"));
    }

    @Test
    @WithMockUser
    void shouldValidateCoupon() throws Exception {
        // Given
        String code = "TEST10";
        Double cartTotal = 150.0;
        DiscountResult result = new DiscountResult(true, 15.0, "Cupón válido", code);

        when(discountService.validateCoupon(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/v1/discounts/validate/{code}", code)
                        .param("cartTotal", String.valueOf(cartTotal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(15.0));
    }
}