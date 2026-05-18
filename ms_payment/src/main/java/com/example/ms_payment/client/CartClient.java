package com.example.ms_payment.client;

import com.example.ms_payment.dto.CartDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CartClient {

    private final WebClient webClient;

    @Autowired
    public CartClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://MS-CART")
                .build();
    }

    public CartDTO getCartByUserId(Long userId) {
        log.info("Consultando carrito del usuario: {}", userId);
        try {
            CartDTO cart = webClient.get()
                    .uri("/api/v1/cart/admin/user/{userId}", userId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            response -> Mono.error(new RuntimeException("Carrito no encontrado")))
                    .bodyToMono(CartDTO.class)
                    .block();

            log.debug("Carrito obtenido: {}", cart);
            return cart;
        } catch (Exception e) {
            log.error("Error al consultar carrito del usuario {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener información del carrito: " + e.getMessage());
        }
    }

    public void clearCart(Long userId) {
        log.info("Limpiando carrito del usuario: {}", userId);
        try {
            webClient.delete()
                    .uri("/api/v1/cart/admin/user/{userId}", userId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Carrito limpiado para usuario: {}", userId);
        } catch (Exception e) {
            log.error("Error al limpiar carrito del usuario {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("No se pudo limpiar el carrito: " + e.getMessage());
        }
    }
}
