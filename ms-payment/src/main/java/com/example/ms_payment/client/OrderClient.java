package com.example.ms_payment.client;

import com.example.ms_payment.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OrderClient {

    private final WebClient webClient;

    @Autowired
    public OrderClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://ms-order")
                .build();
    }

    public OrderDTO getOrderById(Long orderId) {
        log.info("Consultando orden con id: {} en ms-order", orderId);
        try {
            OrderDTO order = webClient.get()
                    .uri("/api/v1/orders/exists/{id}", orderId)
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

    public void updateOrderPaymentStatus(Long orderId, String paymentStatus) {
        log.info("Actualizando estado de pago de la orden {} a {}", orderId, paymentStatus);
        try {
            webClient.patch()
                    .uri("/api/v1/orders/{id}/payment-status?status={status}", orderId, paymentStatus)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Estado de pago actualizado para orden {}", orderId);
        } catch (Exception e) {
            log.error("Error al actualizar estado de pago de la orden {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("No se pudo actualizar el estado de la orden: " + e.getMessage());
        }
    }
}
