package com.example.ms_cart.controller;

import com.example.ms_cart.dto.request.AddItemRequest;
import com.example.ms_cart.dto.response.CartItemResponseDTO;
import com.example.ms_cart.dto.response.CartResponseDTO;
import com.example.ms_cart.model.Cart;
import com.example.ms_cart.model.CartItem;
import com.example.ms_cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Carrito de Compras",
        description = "Endpoints para gestionar el carrito del usuario autenticado"
)
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "Obtener mi carrito",
            description = "Retorna el carrito actual del usuario autenticado con todos sus ítems y el total."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito encontrado con exito."),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado (debería crearse automáticamente)")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<CartResponseDTO> getMyCart(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo carrito del usuario: {}", username);
        Cart cart = cartService.getUserCart(username);
        CartResponseDTO response = mapToResponseDTO(cart);
        addHateoasLinks(response, cart.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @Operation(
            summary = "Agregar item al carrito",
            description = "Añade un producto al carrito del usuario. Si ya existe, incrementa la cantidad."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item añadido correctamente."),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. Cantidad <= 0)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Producto o usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<CartResponseDTO> addItem(Authentication authentication,
                                                   @Valid @RequestBody AddItemRequest request) {
        String username = authentication.getName();
        log.info("Agregando item al carrito del usuario: {}, producto: {}", username, request.getProductId());
        Cart updatedCart = cartService.addItem(username, request);
        CartResponseDTO response = mapToResponseDTO(updatedCart);
        addHateoasLinks(response, updatedCart.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/items/{productId}")
    @Operation(
            summary = "Eliminar item del carrito",
            description = "Remueve completamente un producto del carrito del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item eliminado correctamente. Se retorna el carrito actualizado."),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<CartResponseDTO> removeItem(Authentication authentication,
                                                      @PathVariable Long productId) {
        String username = authentication.getName();
        log.info("Eliminando producto {} del carrito de {}", productId, username);
        Cart updatedCart = cartService.removeItem(username, productId);
        CartResponseDTO response = mapToResponseDTO(updatedCart);
        addHateoasLinks(response, updatedCart.getUserId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(
            summary = "Limpiar carrito",
            description = "Elimina todos los ítems del carrito del usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carrito vaciado con exito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        String username = authentication.getName();
        log.info("Limpiando carrito del usuario: {}", username);
        cartService.clearCart(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{productId}")
    @Operation(
            summary = "Actualizar cantidad de un item",
            description = "Cambia la cantidad de un producto específico en el carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada, se retorna el carrito"),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida (debe ser >= 1)"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<CartResponseDTO> updateQuantity(Authentication authentication,
                                                          @PathVariable Long productId,
                                                          @RequestParam Integer quantity) {
        String username = authentication.getName();
        log.info("Actualizando cantidad del producto {} a {} para usuario {}", productId, quantity, username);
        Cart updatedCart = cartService.updateItemQuantity(username, productId, quantity);
        CartResponseDTO response = mapToResponseDTO(updatedCart);
        addHateoasLinks(response, updatedCart.getUserId());
        return ResponseEntity.ok(response);
    }

    private void addHateoasLinks(CartResponseDTO response, Long userId) {
        // Self link
        Link selfLink = linkTo(methodOn(CartController.class).getMyCart(null)).withSelfRel();
        response.add(selfLink);

        // Link para agregar item
        Link addItemLink = linkTo(methodOn(CartController.class).addItem(null, null))
                .withRel("add-item");
        response.add(addItemLink);

        // Link para limpiar carrito
        Link clearLink = linkTo(methodOn(CartController.class).clearCart(null))
                .withRel("clear");
        response.add(clearLink);

        // Link para obtener carrito por admin (si es ADMIN)
        try {
            Link adminLink = linkTo(methodOn(CartAdminController.class).getUserCart(userId))
                    .withRel("admin-view");
            response.add(adminLink);
        } catch (Exception e) {
            // Si el endpoint admin no está disponible, no agregar el link
            log.debug("No se pudo generar link admin-view para userId: {}", userId);
        }
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