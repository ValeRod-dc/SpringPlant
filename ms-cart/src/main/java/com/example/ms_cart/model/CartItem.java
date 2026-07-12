package com.example.ms_cart.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cart_items")
@Schema(description = "Representa un producto dentro de un carrito de compras")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único del ítem en el carrito",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long idItem;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    @Schema(hidden = true)
    private Cart cart;

    @Column(nullable = false)
    @Schema(
            description = "Identificador del producto (referencia al catálogo de productos)",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long productId;

    @Column(nullable = false)
    @Schema(
            description = "Cantidad del producto en el carrito",
            example = "3",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer quantity;

    @Column(nullable = false)
    @Schema(
            description = "Precio unitario del producto en el momento de agregarlo al carrito",
            example = "2980",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double unitPrice;

    @Column(nullable = false)
    @Schema(
            description = "Subtotal calculado (quantity * unitPrice).",
            example = "8990",
            accessMode = Schema.AccessMode.READ_ONLY  // Si lo calculas en backend
    )
    private Double subtotal;

    @PrePersist
    @PreUpdate
    private void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = quantity * unitPrice;
        }
    }
}