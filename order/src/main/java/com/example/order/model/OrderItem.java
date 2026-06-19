package com.example.order.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa un ítem dentro de una orden de compra")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único del ítem",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "Identificador del producto asociado al ítem",
            example = "10"
    )
    private Long productId;

    @Schema(
            description = "Cantidad del producto en la orden",
            example = "3"
    )
    private Integer quantity;

    @Schema(
            description = "Precio unitario del producto al momento de la compra",
            example = "9990.0"
    )
    private Double price;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    @Schema(
            description = "Orden a la que pertenece este ítem",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Order order;
}