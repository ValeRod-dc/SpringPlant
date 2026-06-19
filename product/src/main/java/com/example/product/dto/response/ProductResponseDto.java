package com.example.product.dto.response;

import com.example.product.model.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto extends RepresentationModel<ProductResponseDto> {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private Category category;
    private CareLevel careLevel;
    private Size size;
    private WateringFrequency wateringFrequency;
    private ProductStatus productStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}