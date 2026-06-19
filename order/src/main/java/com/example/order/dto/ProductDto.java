package com.example.order.dto;

import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String nombre;
    private String categoria;
    private Double precio;
    private String descripcion;
    //private String imagenUrl;
    private Integer stock;
}