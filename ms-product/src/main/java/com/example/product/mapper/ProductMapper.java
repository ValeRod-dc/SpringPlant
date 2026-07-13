package com.example.ms_product.mapper;

import com.example.ms_product.dto.request.ProductRequestDto;
import com.example.ms_product.dto.response.ProductResponseDto;
import com.example.ms_product.model.Product;

public class ProductMapper {

    public Product toEntity(ProductRequestDto dto) {
        Product product = new Product();
        if (dto != null) {
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setStock(dto.getStock());
            product.setCareLevel(dto.getCareLevel());
            product.setCategory(dto.getCategory());
            product.setProductStatus(dto.getProductStatus());
            product.setWateringFrequency(dto.getWateringFrequency());
            product.setSize(dto.getSize());
        }
        return product;
    }

    public ProductResponseDto toResponseDto(Product product) {
        if (product == null) return null;
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .careLevel(product.getCareLevel())
                .size(product.getSize())
                .wateringFrequency(product.getWateringFrequency())
                .productStatus(product.getProductStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
