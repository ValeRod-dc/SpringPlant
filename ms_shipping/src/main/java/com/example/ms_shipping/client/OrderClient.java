package com.example.ms_shipping.client;

import com.example.ms_shipping.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OrderClient {

    private final WebClient webClient;

    @Autowired
    public OrderClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://order")
                .build();
    }

    private String getAuthToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            log.debug("Token encontrado: {}", authHeader != null ? "Sí" : "No");
            return authHeader;
        }
        return null;
    }

    public OrderDTO getOrderById(Long orderId) {
        log.info("Consultando orden con id: {} en ms-orders", orderId);
        try {
            String token = getAuthToken();

            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri("/api/v1/orders/{id}", orderId);

            // Agregar el token si existe
            if (token != null && !token.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, token);
            }

            OrderDTO order = request
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            response -> Mono.error(new RuntimeException("Orden no encontrada")))
                    .bodyToMono(OrderDTO.class)
                    .block();

            log.debug("Orden obtenida: {}", order);
            return order;
        } catch (Exception e) {
            log.error("Error al consultar orden {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener información de la orden: " + e.getMessage());
        }
    }

    public boolean orderExists(Long orderId) {
        log.debug("Verificando existencia de orden: {}", orderId);
        try {
            String token = getAuthToken();

            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri("/api/v1/orders/exists/{id}", orderId);

            if (token != null && !token.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, token);
            }

            java.util.Map response = request
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .block();

            boolean exists = response != null && (Boolean) response.getOrDefault("exists", false);
            log.debug("Orden {} - Existe: {}", orderId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error verificando orden {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }
}