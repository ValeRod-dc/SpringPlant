package com.example.ms_cart.controller;

import com.example.ms_cart.dto.response.CartItemResponseDTO;
import com.example.ms_cart.dto.response.CartResponseDTO;
import com.example.ms_cart.model.Cart;
import com.example.ms_cart.model.CartItem;
import com.example.ms_cart.model.CartStatus;
import com.example.ms_cart.security.filter.JwtAuthFilter;
import com.example.ms_cart.security.jwt.JwtService;
import com.example.ms_cart.service.CartService;
import com.example.ms_cart.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldGetUserCart() throws Exception {
        // Given
        Long userId = 1L;
        List<CartItem> items = new ArrayList<>();
        CartItem item = CartItem.builder()
                .idItem(1L)
                .productId(101L)
                .quantity(2)
                .unitPrice(15000.0)
                .subtotal(30000.0)
                .build();
        items.add(item);

        Cart cart = Cart.builder()
                .cartId(1L)
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .items(items)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.findByUserIdOrThrow(anyLong())).thenReturn(cart);

        // When & Then
        mockMvc.perform(get("/api/v1/cart/admin/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(101))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.admin-clear.href").exists())
                .andExpect(jsonPath("$._links.admin-exists.href").exists());

        verify(cartService).findByUserIdOrThrow(anyLong());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldClearUserCart() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(cartService).clearCartByUserId(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/v1/cart/admin/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cartService).clearCartByUserId(anyLong());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCheckCartExists() throws Exception {
        // Given
        Long userId = 1L;
        when(cartService.cartExists(anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/cart/admin/exists/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));

        verify(cartService).cartExists(anyLong());
    }
}