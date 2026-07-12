package com.example.ms_product.dto.request;

import com.example.ms_product.model.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductRequestDto {

    @NotBlank(message = "Requiere un nombre")
    private String name;

    @NotBlank(message = "Requiere una descripcion")
    private String description;

    @NotNull(message = "Requiere un precio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double price;

    @NotNull(message = "Requiere un stock")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "Care Level invalido. Solo se acepta: LOW, MEDIUM, HIGH")
    private CareLevel careLevel;

    @NotNull(message = "Category invalida. Solo se acepta: COLLECTION, INTERIOR, EXTERIOR, SUCCULENT, TREE")
    private Category category;

    @NotNull(message = "Product status invalido. Solo se acepta: INACTIVE, ACTIVE, OUT_OF_STOCK, SEASONAL")
    private ProductStatus productStatus;

    @NotNull(message = "Watering frequency invalido. Solo se acepta: DAILY, WEEKLY, BIWEEKLY")
    private WateringFrequency wateringFrequency;

    @NotNull(message = "Size invalido. Solo se acepta: SMALL, MEDIUM, BIG")
    private Size size;
}