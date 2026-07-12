package com.example.ms_inventory.model;

import com.example.ms_inventory.model.enums.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa un movimiento de inventario en el sistema")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único del movimiento",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(nullable = false)
    @Schema(
            description = "Identificador del inventario asociado al movimiento",
            example = "3"
    )
    private Long inventoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Tipo de movimiento realizado sobre el inventario",
            example = "IN",
            allowableValues = {"IN", "OUT", "RESERVED"}
    )
    private MovementType type;

    @Column(nullable = false)
    @Schema(
            description = "Cantidad de unidades involucradas en el movimiento",
            example = "10"
    )
    private Integer quantity;

    @Schema(
            description = "Fecha y hora en que se realizó el movimiento",
            example = "2024-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime movedAt;

    @PrePersist
    public void prePersist() {
        this.movedAt = LocalDateTime.now();
    }
}