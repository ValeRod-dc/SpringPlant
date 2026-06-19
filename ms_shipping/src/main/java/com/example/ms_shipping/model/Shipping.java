package com.example.ms_shipping.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_shippings")
@Schema(description = "Entidad que representa un envío")
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del envío",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long shippingId;

    @Schema(description = "ID de la orden asociada",
            example = "1001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(nullable = false)
    private Long orderId;

    @Schema(description = "ID del usuario que recibe el envío",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @Schema(description = "Dirección de envío",
            example = "Calle Falsa 123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(nullable = false)
    private String address;

    @Schema(description = "Estado actual del envío",
            example = "PENDING",
            allowableValues = {"PENDING", "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "CANCELLED"})
    @Enumerated(EnumType.STRING)
    private ShippingStatus status;

    @Schema(description = "Número de seguimiento",
            example = "TRKABC123",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String trackingNumber;

    @Schema(description = "Fecha de creación",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha en que fue enviado",
            example = "2026-02-01T10:00:00")
    private LocalDateTime shippedAt;

    @Schema(description = "Fecha estimada de entrega",
            example = "2026-02-06T18:00:00")
    private LocalDateTime estimatedDelivery;

    @Schema(description = "Fecha de entrega real",
            example = "2026-02-05T14:30:00")
    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = ShippingStatus.PENDING;
        if (this.trackingNumber == null) {
            this.trackingNumber = "TRK" + System.currentTimeMillis();
        }
    }
}