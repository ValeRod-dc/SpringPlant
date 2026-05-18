package com.example.ms_cart.controller;

import com.example.ms_cart.dto.response.CartItemResponseDTO;
import com.example.ms_cart.dto.response.CartResponseDTO;
import com.example.ms_cart.model.Cart;
import com.example.ms_cart.model.CartItem;
import com.example.ms_cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart/admin")
@RequiredArgsConstructor
public class CartAdminController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CartResponseDTO> getUserCart(@PathVariable Long userId) {
        log.info("ADMIN - Obteniendo carrito del usuario {}", userId);
        Cart cart = cartService.findByUserIdOrThrow(userId);
        return ResponseEntity.ok(mapToResponseDTO(cart));
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearUserCart(@PathVariable Long userId) {
        log.info("ADMIN - Limpiando carrito del usuario {}", userId);
        Cart cart = cartService.findByUserIdOrThrow(userId);
        cart.getItems().clear();
        cartService.findByIdOrThrow(cart.getCartId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> cartExists(@PathVariable Long userId) {
        log.debug("ADMIN - Verificando existencia de carrito para usuario: {}", userId);
        boolean exists = cartService.cartExists(userId);
        return ResponseEntity.ok(Map.of("exists", exists));
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