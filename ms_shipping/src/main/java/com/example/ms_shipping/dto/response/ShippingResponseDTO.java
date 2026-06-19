package com.example.ms_shipping.dto.response;

import com.example.ms_shipping.model.ShippingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Respuesta con datos de un envío")
public class ShippingResponseDTO {

    @Schema(description = "ID del envío",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long shippingId;

    @Schema(description = "ID de la orden",
            example = "1001")
    private Long orderId;

    @Schema(description = "ID del usuario",
            example = "1")
    private Long userId;

    @Schema(description = "Dirección de envío",
            example = "Calle Falsa 123")
    private String address;

    @Schema(description = "Estado del envío",
            example = "PENDING")
    private ShippingStatus status;

    @Schema(description = "Número de seguimiento",
            example = "TRKABC123")
    private String trackingNumber;

    @Schema(description = "Fecha de creación",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de envío",
            example = "2026-02-01T10:00:00")
    private LocalDateTime shippedAt;

    @Schema(description = "Fecha estimada de entrega",
            example = "2026-02-05T18:00:00")
    private LocalDateTime estimatedDelivery;

    @Schema(description = "Fecha de entrega real",
            example = "2026-02-04T14:30:00")
    private LocalDateTime deliveredAt;
}