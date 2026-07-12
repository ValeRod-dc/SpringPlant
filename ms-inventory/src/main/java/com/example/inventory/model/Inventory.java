package com.example.ms_inventory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa el inventario de un producto en tienda")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único del inventario",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(nullable = false)
    @Schema(
            description = "Identificador del producto asociado al inventario",
            example = "10"
    )
    private Long productId;

    @Column(nullable = false)
    @Schema(
            description = "Cantidad disponible del producto en stock",
            example = "50"
    )
    private Integer quantityAvailable;

    @Column(nullable = false)
    @Schema(
            description = "Cantidad reservada del producto pendiente de despacho",
            example = "5"
    )
    private Integer quantityReserved;

    @Column(nullable = false)
    @Schema(
            description = "Ubicación de la tienda donde se almacena el producto",
            example = "Bodega Santiago Centro"
    )
    private String storeLocation;

    @Schema(
            description = "Fecha y hora del último reabastecimiento del producto",
            example = "2024-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime lastRestockedAt;

    @PrePersist
    public void prePersist() {
        this.lastRestockedAt = LocalDateTime.now();
    }
}