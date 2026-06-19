package com.example.ms_cart.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "carts")
@Schema(description = "Entidad que representa a un carrito de compras :b")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único del carrito",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long cartId;

    @Schema(
            description = "Identificador único del cliente",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Column(nullable = false)
    private Long userId;

    @Schema(
            description = "Estado del carrito",
            allowableValues = {"ACTIVE", "CHECKED_OUT", "ABANDONED"}
    )
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CartStatus status;

    @Schema(
            description = "Fecha y hora de creación",
            example = "2026-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Schema(
            description = "Fecha y hora de la última actualización",
            example = "2026-01-20T14:45:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(
            description = "Lista de productos en el carrito",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<CartItem> items = new ArrayList<>();

    @Schema(
            description = "Monto total del carrito (suma de cantidad * precio unitario de cada ítem)",
            example = "5890",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    public Double getTotal() {
        if (items == null) return 0.0;
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}