package com.example.ms_cart.controller;

import com.example.ms_cart.dto.response.CartItemResponseDTO;
import com.example.ms_cart.dto.response.CartResponseDTO;
import com.example.ms_cart.model.Cart;
import com.example.ms_cart.model.CartItem;
import com.example.ms_cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Administración de Carritos",
        description = "Endpoints para que administradores gestionen los carritos de usuarios"
)
public class CartAdminController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Obtener carrito de un usuario por su ID",
            description = "Retorna el carrito completo de cualquier usuario (solo para ADMIN)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de ADMIN"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene carrito (o no existe)")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CartResponseDTO> getUserCart(@PathVariable Long userId) {
        log.info("ADMIN - Obteniendo carrito del usuario {}", userId);
        Cart cart = cartService.findByUserIdOrThrow(userId);
        CartResponseDTO response = mapToResponseDTO(cart);
        addHateoasLinksAdmin(response, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{userId}")
    @Operation(
            summary = "Limpiar carrito de un usuario",
            description = "Elimina todos los ítems del carrito de un usuario específico (solo ADMIN)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carrito limpiado exitosamente (sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de ADMIN"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearUserCart(@PathVariable Long userId) {
        log.info("ADMIN - Limpiando carrito del usuario {}", userId);
        cartService.clearCartByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{userId}")
    @Operation(
            summary = "Verificar la existencia de un carrito",
            description = "Indica si un usuario tiene un carrito asociado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Respuesta con booleano 'exists'"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> cartExists(@Parameter(description = "ID del usuario", example = "1001")
                                                           @PathVariable Long userId) {
        log.debug("ADMIN - Verificando existencia de carrito para usuario: {}", userId);
        boolean exists = cartService.cartExists(userId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    private void addHateoasLinksAdmin(CartResponseDTO response, Long userId) {
        // Self link
        Link selfLink = linkTo(methodOn(CartAdminController.class).getUserCart(userId))
                .withSelfRel();
        response.add(selfLink);

        // Link para limpiar carrito (admin)
        Link clearLink = linkTo(methodOn(CartAdminController.class).clearUserCart(userId))
                .withRel("admin-clear");
        response.add(clearLink);

        // Link para verificar existencia
        Link existsLink = linkTo(methodOn(CartAdminController.class).cartExists(userId))
                .withRel("admin-exists");
        response.add(existsLink);

        // Link al carrito del usuario (ruta normal)
        Link userCartLink = linkTo(methodOn(CartController.class).getMyCart(null))
                .withRel("user-cart");
        response.add(userCartLink);
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