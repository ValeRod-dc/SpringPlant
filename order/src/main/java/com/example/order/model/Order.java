package com.example.order.model;

import com.example.order.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa una orden de compra en el sistema")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único de la orden",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "Identificador del cliente que realizó la orden",
            example = "5"
    )
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Schema(
            description = "Estado actual de la orden",
            example = "PENDING",
            allowableValues = {"PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"}
    )
    private OrderStatus status;

    @Schema(
            description = "Identificador del pago asociado a la orden",
            example = "PAY-123456789"
    )
    private String paymentId;

    @Schema(
            description = "Estado del pago de la orden",
            example = "PAID",
            allowableValues = {"PENDING", "PAID", "FAILED", "REFUNDED"}
    )
    private String paymentStatus;

    @Schema(
            description = "Monto total de la orden en pesos chilenos",
            example = "29990.0"
    )
    private Double totalAmount;

    @CreationTimestamp
    @Schema(
            description = "Fecha y hora de creación de la orden",
            example = "2024-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(
            description = "Fecha y hora de la última actualización de la orden",
            example = "2024-01-20T14:45:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Schema(description = "Lista de productos incluidos en la orden")
    private List<OrderItem> items = new ArrayList<>();
}