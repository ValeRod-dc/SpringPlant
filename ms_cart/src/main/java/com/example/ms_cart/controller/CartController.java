package com.example.ms_cart.controller;

import com.example.ms_cart.dto.request.AddItemRequest;
import com.example.ms_cart.dto.response.CartItemResponseDTO;
import com.example.ms_cart.dto.response.CartResponseDTO;
import com.example.ms_cart.model.Cart;
import com.example.ms_cart.model.CartItem;
import com.example.ms_cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Obtener mi carrito
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<CartResponseDTO> getMyCart(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo carrito del usuario: {}", username);
        Cart cart = cartService.getUserCart(username);
        return ResponseEntity.ok(mapToResponseDTO(cart));
    }

    // Agregar item al carrito
    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<CartResponseDTO> addItem(Authentication authentication,
                                                   @Valid @RequestBody AddItemRequest request) {
        String username = authentication.getName();
        log.info("Agregando item al carrito del usuario: {}, producto: {}", username, request.getProductId());
        Cart updatedCart = cartService.addItem(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(updatedCart));
    }

    // Eliminar item del carrito
    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<CartResponseDTO> removeItem(Authentication authentication,
                                                      @PathVariable Long productId) {
        String username = authentication.getName();
        log.info("Eliminando producto {} del carrito de {}", productId, username);
        Cart updatedCart = cartService.removeItem(username, productId);
        return ResponseEntity.ok(mapToResponseDTO(updatedCart));
    }

    // Limpiar carrito
    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        String username = authentication.getName();
        log.info("Limpiando carrito del usuario: {}", username);
        cartService.clearCart(username);
        return ResponseEntity.noContent().build();
    }

    // Actualizar cantidad de un item
    @PutMapping("/items/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<CartResponseDTO> updateQuantity(Authentication authentication,
                                                          @PathVariable Long productId,
                                                          @RequestParam Integer quantity) {
        String username = authentication.getName();
        log.info("Actualizando cantidad del producto {} a {} para usuario {}", productId, quantity, username);
        Cart updatedCart = cartService.updateItemQuantity(username, productId, quantity);
        return ResponseEntity.ok(mapToResponseDTO(updatedCart));
    }

    private CartResponseDTO mapToResponseDTO(Cart cart) {
        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .items(cart.getItems().stream()
                        .map(this::mapToItemDTO)
                        .collect(Collectors.toList()))
                .total(cart.getTotal())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponseDTO mapToItemDTO(CartItem item) {
        return CartItemResponseDTO.builder()
                .productId(item.getProductId())
                .productName("Desconocido")
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}