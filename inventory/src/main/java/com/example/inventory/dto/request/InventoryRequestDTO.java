package com.example.inventory.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestDTO {

    @NotNull
    private Long productId;

    @NotNull
    @PositiveOrZero
    private Integer quantityAvailable;

    @NotNull
    @PositiveOrZero
    private Integer quantityReserved;

    @NotBlank
    private String storeLocation;

}