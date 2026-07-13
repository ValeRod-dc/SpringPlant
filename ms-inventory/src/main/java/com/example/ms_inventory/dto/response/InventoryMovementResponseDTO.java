package com.example.ms_inventory.dto.response;

import com.example.ms_inventory.model.enums.MovementType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryMovementResponseDTO {

    private Long id;
    private Long inventoryId;
    private MovementType type;
    private Integer quantity;
    private LocalDateTime movedAt;
}