package com.example.ms_cart.client;

import com.example.ms_cart.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProductServiceClient {

    private final WebClient webClient;

    @Autowired
    public ProductServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://ms-product")
                .build();
    }

    public ProductDTO getProductById(Long productId) {
        log.info("Consultando producto con id: {} en ms-product", productId);
        try {
            ProductDTO product = webClient.get()
                    .uri("/api/v1/productos/{id}", productId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            response -> Mono.error(new RuntimeException("Producto no encontrado")))
                    .bodyToMono(ProductDTO.class)
                    .block();

            log.debug("Producto obtenido: {}", product);
            return product;
        } catch (Exception e) {
            log.error("Error al consultar producto {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener información del producto: " + e.getMessage());
        }
    }
}