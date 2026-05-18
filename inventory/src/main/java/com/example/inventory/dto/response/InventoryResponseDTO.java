package com.example.inventory.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryResponseDTO {

    private Long id;
    private Long productId;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private String storeLocation;
    private LocalDateTime lastRestockedAt;
}
