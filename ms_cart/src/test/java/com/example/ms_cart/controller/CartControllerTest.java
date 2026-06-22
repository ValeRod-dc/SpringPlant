package com.example.ms_cart.controller;

import com.example.ms_cart.dto.request.AddItemRequest;
import com.example.ms_cart.dto.response.CartItemResponseDTO;
import com.example.ms_cart.dto.response.CartResponseDTO;
import com.example.ms_cart.model.Cart;
import com.example.ms_cart.model.CartItem;
import com.example.ms_cart.model.CartStatus;
import com.example.ms_cart.security.filter.JwtAuthFilter;
import com.example.ms_cart.security.jwt.JwtService;
import com.example.ms_cart.service.CartService;
import com.example.ms_cart.service.CustomUserDetailsService;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(username = "testuser", roles = "CLIENT")
    void shouldGetMyCart() throws Exception {
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

        when(cartService.getUserCart(anyString())).thenReturn(cart);

        // When & Then
        mockMvc.perform(get("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(101))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(30000.0))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.add-item.href").exists())
                .andExpect(jsonPath("$._links.clear.href").exists());

        verify(cartService).getUserCart(anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CLIENT")
    void shouldAddItemToCart() throws Exception {
        // Given
        AddItemRequest request = new AddItemRequest();
        request.setProductId(101L);
        request.setQuantity(2);

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
                .userId(1L)
                .status(CartStatus.ACTIVE)
                .items(items)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.addItem(anyString(), any(AddItemRequest.class))).thenReturn(cart);

        // When & Then
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(101))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.add-item.href").exists());

        verify(cartService).addItem(anyString(), any(AddItemRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CLIENT")
    void shouldReturn400WhenAddItemHasInvalidQuantity() throws Exception {
        // Given
        String json = """
                {
                    "productId": 101,
                    "quantity": 0
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CLIENT")
    void shouldReturn404WhenCartNotFound() throws Exception {
        // Given
        when(cartService.getUserCart(anyString()))
                .thenThrow(new RuntimeException("Cart not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CLIENT")
    void shouldRemoveItemFromCart() throws Exception {
        // Given
        Long productId = 101L;
        List<CartItem> items = new ArrayList<>();
        Cart cart = Cart.builder()
                .cartId(1L)
                .userId(1L)
                .status(CartStatus.ACTIVE)
                .items(items)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.removeItem(anyString(), anyLong())).thenReturn(cart);

        // When & Then
        mockMvc.perform(delete("/api/v1/cart/items/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(cartService).removeItem(anyString(), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CLIENT")
    void shouldClearCart() throws Exception {
        // Given
        doNothing().when(cartService).clearCart(anyString());

        // When & Then
        mockMvc.perform(delete("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart(anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CLIENT")
    void shouldUpdateItemQuantity() throws Exception {
        // Given
        Long productId = 101L;
        Integer newQuantity = 5;
        List<CartItem> items = new ArrayList<>();
        CartItem item = CartItem.builder()
                .idItem(1L)
                .productId(productId)
                .quantity(newQuantity)
                .unitPrice(15000.0)
                .subtotal(75000.0)
                .build();
        items.add(item);

        Cart cart = Cart.builder()
                .cartId(1L)
                .userId(1L)
                .status(CartStatus.ACTIVE)
                .items(items)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.updateItemQuantity(anyString(), anyLong(), anyInt())).thenReturn(cart);

        // When & Then
        mockMvc.perform(put("/api/v1/cart/items/{productId}", productId)
                        .param("quantity", newQuantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(newQuantity))
                .andExpect(jsonPath("$.items[0].subtotal").value(75000.0))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(cartService).updateItemQuantity(anyString(), anyLong(), anyInt());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CLIENT")
    void shouldReturn400WhenUpdateQuantityInvalid() throws Exception {
        // Given
        Long productId = 101L;
        Integer invalidQuantity = -1;

        // When & Then
        mockMvc.perform(put("/api/v1/cart/items/{productId}", productId)
                        .param("quantity", invalidQuantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}