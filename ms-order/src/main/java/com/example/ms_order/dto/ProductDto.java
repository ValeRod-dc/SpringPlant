package com.example.ms_order.dto;

import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private String description;
    private Integer stock;
}