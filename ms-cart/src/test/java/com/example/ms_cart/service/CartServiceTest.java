package com.example.ms_cart.service;

import com.example.ms_cart.client.ProductServiceClient;
import com.example.ms_cart.client.UserClient;
import com.example.ms_cart.dto.ProductDTO;
import com.example.ms_cart.dto.request.AddItemRequest;
import com.example.ms_cart.exception.custom.*;
import com.example.ms_cart.model.*;
import com.example.ms_cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    private final String USERNAME = "testuser";
    private final Long USER_ID = 1L;
    private final Long PRODUCT_ID = 101L;
    private final int QUANTITY = 2;
    private final int STOCK_AVAILABLE = 10;
    private final double PRODUCT_PRICE = 25.0;

    private Cart existingCart;
    private CartItem existingItem;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        // Producto simulado
        productDTO = new ProductDTO();
        productDTO.setId(PRODUCT_ID);
        productDTO.setNombre("Producto Test");
        productDTO.setPrecio(PRODUCT_PRICE);
        productDTO.setStock(STOCK_AVAILABLE);

        // Carrito existente con un item
        existingCart = Cart.builder()
                .cartId(1L)
                .userId(USER_ID)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        existingItem = CartItem.builder()
                .cart(existingCart)
                .productId(PRODUCT_ID)
                .quantity(1)
                .unitPrice(PRODUCT_PRICE)
                .subtotal(PRODUCT_PRICE)
                .build();
        existingCart.getItems().add(existingItem);
    }

    // ========== TESTS DE addItem ==========

    @Test
    void shouldAddItemWhenCartExistsAndProductValid() {
        // Arrange
        when(userClient.userExists(USERNAME)).thenReturn(true);
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(productDTO);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setQuantity(QUANTITY);

        // Act
        Cart result = cartService.addItem(USERNAME, request);

        // Assert
        assertNotNull(result);
        CartItem updatedItem = result.getItems().stream()
                .filter(item -> item.getProductId().equals(PRODUCT_ID))
                .findFirst().orElseThrow();
        assertEquals(1 + QUANTITY, updatedItem.getQuantity()); // 1 existente + 2 nuevos
        assertEquals(PRODUCT_PRICE * (1 + QUANTITY), updatedItem.getSubtotal());
        verify(cartRepository).save(existingCart);
    }

    @Test
    void shouldCreateCartWhenUserDoesNotHaveOne() {
        // Arrange
        when(userClient.userExists(USERNAME)).thenReturn(true);
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(productDTO);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Cart newCart = Cart.builder()
                .cartId(2L)
                .userId(USER_ID)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setQuantity(QUANTITY);

        // Act
        Cart result = cartService.addItem(USERNAME, request);

        // Assert
        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldThrowUserNameNotFoundExceptionWhenUserNameDoesNotExist() {
        when(userClient.userExists("testuser")).thenReturn(false);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(101L);
        request.setQuantity(1);

        assertThrows(UserNotFoundException.class,
                () -> cartService.addItem("testuser", request));

        verify(userClient, never()).getUserIdByUsername(anyString());
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenProductNotFound() {
        // Arrange
        when(userClient.userExists(USERNAME)).thenReturn(true);
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(productClient.getProductById(PRODUCT_ID))
                .thenThrow(new RuntimeException("Producto no encontrado"));

        AddItemRequest request = new AddItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setQuantity(QUANTITY);

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> cartService.addItem(USERNAME, request));

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldThrowInsufficientStockExceptionWhenStockNotEnough() {
        // Arrange
        int requestedQuantity = 15;
        productDTO.setStock(10);

        when(userClient.userExists(USERNAME)).thenReturn(true);
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(productDTO);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setQuantity(requestedQuantity);

        // Act & Assert
        assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(USERNAME, request));

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldThrowInsufficientStockExceptionWhenUpdatingExistingItemExceedsStock() {
        // Arrange
        int newTotal = 15;
        productDTO.setStock(10);

        when(userClient.userExists(USERNAME)).thenReturn(true);
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(productDTO);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));

        AddItemRequest request = new AddItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setQuantity(newTotal); // 15 > stock 10

        // Act & Assert
        assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(USERNAME, request));

        verify(cartRepository, never()).save(any());
    }

    // ========== TESTS DE removeItem ==========

    @Test
    void shouldRemoveItemFromCart() {
        // Arrange
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        Cart result = cartService.removeItem(USERNAME, PRODUCT_ID);

        // Assert
        assertTrue(result.getItems().isEmpty());
        verify(cartRepository).save(existingCart);
    }

    @Test
    void shouldThrowItemNotFoundExceptionWhenRemovingNonExistentItem() {
        // Arrange
        Long nonExistentProductId = 999L;
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));

        // Act & Assert
        assertThrows(ItemNotFoundException.class,
                () -> cartService.removeItem(USERNAME, nonExistentProductId));

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldThrowCartNotFoundExceptionWhenRemovingFromNonExistentCart() {
        // Arrange
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartNotFoundException.class,
                () -> cartService.removeItem(USERNAME, PRODUCT_ID));

        verify(cartRepository, never()).save(any());
    }

    // ========== TESTS DE clearCart ==========

    @Test
    void shouldClearCart() {
        // Arrange
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        cartService.clearCart(USERNAME);

        // Assert
        assertTrue(existingCart.getItems().isEmpty());
        verify(cartRepository).save(existingCart);
    }

    @Test
    void shouldThrowCartNotFoundExceptionWhenClearingNonExistentCart() {
        // Arrange
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartNotFoundException.class,
                () -> cartService.clearCart(USERNAME));

        verify(cartRepository, never()).save(any());
    }

    // ========== TESTS DE getUserCart ==========

    @Test
    void shouldGetUserCart() {
        // Arrange
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));

        // Act
        Cart result = cartService.getUserCart(USERNAME);

        // Assert
        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        verify(cartRepository).findByUserId(USER_ID);
    }

    @Test
    void shouldThrowCartNotFoundExceptionWhenCartNotFound() {
        // Arrange
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartNotFoundException.class,
                () -> cartService.getUserCart(USERNAME));
    }

    // ========== TESTS DE updateItemQuantity ==========

    @Test
    void shouldUpdateItemQuantity() {
        // Arrange
        int newQuantity = 5;
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(productDTO);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        Cart result = cartService.updateItemQuantity(USERNAME, PRODUCT_ID, newQuantity);

        // Assert
        CartItem updatedItem = result.getItems().stream()
                .filter(item -> item.getProductId().equals(PRODUCT_ID))
                .findFirst().orElseThrow();
        assertEquals(newQuantity, updatedItem.getQuantity());
        assertEquals(PRODUCT_PRICE * newQuantity, updatedItem.getSubtotal());
        verify(cartRepository).save(existingCart);
    }

    @Test
    void shouldRemoveItemWhenQuantityIsZeroOrNegative() {
        // Arrange
        int newQuantity = 0;
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        Cart result = cartService.updateItemQuantity(USERNAME, PRODUCT_ID, newQuantity);

        // Assert
        assertTrue(result.getItems().isEmpty());
        verify(cartRepository).save(existingCart);
    }

    @Test
    void shouldThrowItemNotFoundExceptionWhenUpdatingNonExistentItem() {
        // Arrange
        Long nonExistentProductId = 999L;
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));

        // Act & Assert
        assertThrows(ItemNotFoundException.class,
                () -> cartService.updateItemQuantity(USERNAME, nonExistentProductId, 5));

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldThrowInsufficientStockExceptionWhenUpdateQuantityExceedsStock() {
        // Arrange
        int newQuantity = 15;
        productDTO.setStock(10);
        when(userClient.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(productDTO);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));

        // Act & Assert
        assertThrows(InsufficientStockException.class,
                () -> cartService.updateItemQuantity(USERNAME, PRODUCT_ID, newQuantity));

        verify(cartRepository, never()).save(any());
    }
}