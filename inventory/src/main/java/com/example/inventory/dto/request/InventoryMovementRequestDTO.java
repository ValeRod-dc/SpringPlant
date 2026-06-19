package com.example.inventory.dto.request;

import com.example.inventory.model.enums.MovementType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryMovementRequestDTO {

    @NotNull
    private Long inventoryId;

    @NotNull
    private MovementType type;

    @NotNull
    private Integer quantity;
}