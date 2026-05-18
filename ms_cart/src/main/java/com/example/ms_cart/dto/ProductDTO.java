package com.example.ms_cart.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String nombre;
    private String categoria;
    private Double precio;
    private String descripcion;
    //private String imagenUrl;
    private Integer stock;
}