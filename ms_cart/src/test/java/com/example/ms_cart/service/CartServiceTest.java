package com.example.ms_cart.service;

import com.example.ms_cart.client.ProductServiceClient;
import com.example.ms_cart.client.UserClient;
import com.example.ms_cart.dto.ProductDTO;
import com.example.ms_cart.dto.request.AddItemRequest;
import com.example.ms_cart.exception.custom.*;
import com.example.ms_cart.model.Cart;
import com.example.ms_cart.model.CartItem;
import com.example.ms_cart.model.CartStatus;
import com.example.ms_cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private CartService cartService;

    private String username;
    private Long userId;
    private AddItemRequest validRequest;
    private ProductDTO validProduct;
    private Cart emptyCart;
    private Cart cartWithItem;

    @BeforeEach
    void setUp() {
        username = "testuser";
        userId = 1L;

        validRequest = new AddItemRequest();
        validRequest.setProductId(101L);
        validRequest.setQuantity(2);

        validProduct = new ProductDTO();
        validProduct.setId(101L);
        validProduct.setNombre("Test Product");
        validProduct.setPrecio(15000.0);
        validProduct.setStock(10);

        emptyCart = Cart.builder()
                .cartId(1L)
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        cartWithItem = Cart.builder()
                .cartId(1L)
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        CartItem item = CartItem.builder()
                .idItem(1L)
                .productId(101L)
                .quantity(2)
                .unitPrice(15000.0)
                .subtotal(30000.0)
                .build();
        cartWithItem.getItems().add(item);
    }

    @Test
    void shouldAddItemWhenCartExistsAndProductValid() {
        // Given
        when(userClient.getUserIdByUsername(username)).thenReturn(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));
        when(productClient.getProductById(101L)).thenReturn(validProduct);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Cart result = cartService.addItem(username, validRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(101L, result.getItems().get(0).getProductId());
        assertEquals(2, result.getItems().get(0).getQuantity());
        assertEquals(15000.0, result.getItems().get(0).getUnitPrice());

        verify(userClient).getUserIdByUsername(username);
        verify(cartRepository).findByUserId(userId);
        verify(productClient).getProductById(101L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldCreateCartWhenUserDoesNotHaveOne() {
        // Given
        when(userClient.getUserIdByUsername(username)).thenReturn(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(productClient.getProductById(101L)).thenReturn(validProduct);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setCartId(1L);
            return cart;
        });

        // When
        Cart result = cartService.addItem(username, validRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getCartId());
        assertEquals(1, result.getItems().size());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Given
        when(userClient.getUserIdByUsername(username)).thenThrow(new UserNotFoundException("User not found"));

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            cartService.addItem(username, validRequest);
        });

        verify(userClient).getUserIdByUsername(username);
        verify(cartRepository, never()).findByUserId(anyLong());
        verify(productClient, never()).getProductById(anyLong());
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenProductNotFound() {
        // Given
        when(userClient.getUserIdByUsername(username)).thenReturn(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));
        when(productClient.getProductById(101L)).thenThrow(new RuntimeException("Product not found"));

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            cartService.addItem(username, validRequest);
        });

        verify(productClient).getProductById(101L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldThrowInsufficientStockExceptionWhenStockNotEnough() {
        // Given
        validProduct.setStock(1);
        when(userClient.getUserIdByUsername(username)).thenReturn(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));
        when(productClient.getProductById(101L)).thenReturn(validProduct);

        // When & Then
        assertThrows(InsufficientStockException.class, () -> {
            cartService.addItem(username, validRequest);
        });

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldRemoveItemFromCart() {
        // Given
        Long productId = 101L;
        when(userClient.getUserIdByUsername(username)).thenReturn(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartWithItem));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Cart result = cartService.removeItem(username, productId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getItems().size());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldThrowItemNotFoundExceptionWhenItemNotInCart() {
        // Given
        Long productId = 999L;
        when(userClient.getUserIdByUsername(username)).thenReturn(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartWithItem));

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> {
            cartService.removeItem(username, productId);
        });

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldClearCart() {
        // Given
        when(userClient.getUserIdByUsername(username)).thenReturn(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartWithItem));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        cartService.clearCart(username);

        // Then
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldFindCartByUserIdOrThrowWhenExists() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));

        // When
        Cart result = cartService.findByUserIdOrThrow(userId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getCartId());

        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void shouldThrowCartNotFoundExceptionWhenCartDoesNotExist() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CartNotFoundException.class, () -> {
            cartService.findByUserIdOrThrow(userId);
        });

        verify(cartRepository).findByUserId(userId);
    }
}