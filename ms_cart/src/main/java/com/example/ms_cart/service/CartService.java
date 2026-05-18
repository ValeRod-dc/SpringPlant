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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productClient;
    private final UserClient userClient;

    public Cart findByIdOrThrow(Long cartId) {
        log.debug("Buscando carrito por ID: {}", cartId);
        return cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("Carrito no encontrado - ID: {}", cartId);
                    return new CartNotFoundException("Carrito no encontrado con ID: " + cartId);
                });
    }

    public Cart findByUserIdOrThrow(Long userId) {
        log.debug("Buscando carrito por usuario ID: {}", userId);
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Carrito no encontrado para usuario ID: {}", userId);
                    return new CartNotFoundException("Carrito no encontrado para el usuario: " + userId);
                });
    }

    public boolean cartExists(Long userId) {
        log.debug("Verificando existencia de carrito para usuario: {}", userId);
        return cartRepository.existsByUserId(userId);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creando nuevo carrito para usuario {}", userId);
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .status(CartStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .items(new java.util.ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public Cart addItem(String username, AddItemRequest request) {
        log.info("Agregando item al carrito del usuario: {}, producto: {}", username, request.getProductId());

        // Validar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // Validar producto en ms_products
        ProductDTO product;
        try {
            product = productClient.getProductById(request.getProductId());
        } catch (Exception e) {
            log.error("Error al obtener producto {}: {}", request.getProductId(), e.getMessage());
            throw new ProductNotFoundException("Producto no encontrado con ID: " + request.getProductId());
        }

        if (product.getStock() < request.getQuantity()) {
            log.warn("Stock insuficiente para producto {}: stock={}, solicitado={}",
                    product.getNombre(), product.getStock(), request.getQuantity());
            throw new InsufficientStockException("Stock insuficiente para el producto " + product.getNombre() +
                    ". Disponible: " + product.getStock());
        }

        // Obtener o crear carrito
        Cart cart = getOrCreateCart(getUserIdFromUsername(username));

        // Buscar si ya existe el item
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException("Stock insuficiente para incrementar cantidad del producto " + product.getNombre());
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setSubtotal(existingItem.getQuantity() * existingItem.getUnitPrice());
            log.debug("Cantidad actualizada para producto {}: nueva cantidad {}", product.getNombre(), newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrecio())
                    .subtotal(product.getPrecio() * request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            log.debug("Nuevo item agregado: {} x {}", product.getNombre(), request.getQuantity());
        }

        Cart saved = cartRepository.save(cart);
        log.info("Carrito actualizado para usuario: {}", username);
        return saved;
    }

    @Transactional
    public Cart removeItem(String username, Long productId) {
        log.info("Eliminando producto {} del carrito del usuario: {}", productId, username);
        Long userId = getUserIdFromUsername(username);
        Cart cart = findByUserIdOrThrow(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            log.warn("Producto {} no encontrado en el carrito del usuario {}", productId, username);
            throw new ItemNotFoundException("Producto no encontrado en el carrito");
        }

        log.debug("Producto eliminado del carrito");
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String username) {
        log.info("Limpiando carrito del usuario: {}", username);
        Long userId = getUserIdFromUsername(username);
        Cart cart = findByUserIdOrThrow(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Carrito limpiado para usuario: {}", username);
    }

    public Cart getUserCart(String username) {
        log.debug("Obteniendo carrito del usuario: {}", username);
        Long userId = getUserIdFromUsername(username);
        return findByUserIdOrThrow(userId);
    }

    @Transactional
    public Cart updateItemQuantity(String username, Long productId, Integer newQuantity) {
        log.info("Actualizando cantidad para producto {} del usuario {} a {}", productId, username, newQuantity);
        Long userId = getUserIdFromUsername(username);
        Cart cart = findByUserIdOrThrow(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Producto no encontrado en el carrito"));

        if (newQuantity <= 0) {
            cart.getItems().remove(item);
            log.debug("Cantidad <=0, item eliminado");
        } else {
            // Validar stock nuevamente
            ProductDTO product;
            try {
                product = productClient.getProductById(productId);
            } catch (Exception e) {
                throw new ProductNotFoundException("Producto no encontrado con ID: " + productId);
            }

            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException("Stock insuficiente para el producto " + product.getNombre());
            }
            item.setQuantity(newQuantity);
            item.setSubtotal(item.getUnitPrice() * newQuantity);
            log.debug("Cantidad actualizada a {}", newQuantity);
        }
        return cartRepository.save(cart);
    }

    // Método temporal para obtener userId desde username
    private Long getUserIdFromUsername(String username) {
        // TODO: Llamar a ms_users para obtener el ID real
        // Por ahora, retornamos un ID fijo para pruebas
        return 1L;
    }
}