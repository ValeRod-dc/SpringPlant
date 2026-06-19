package com.example.ms_cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos del producto obtenidos del servicio de productos")
public class ProductDTO {

    @Schema(description = "ID del producto", example = "101")
    private Long id;

    @Schema(description = "Nombre del producto", example = "Alocasia Bambino Arrow")
    private String nombre;

    @Schema(description = "Categoría del producto", example = "COLLECTION, INTERIOR")
    private String categoria;

    @Schema(description = "Precio por unidad", example = "20000")
    private Double precio;

    @Schema(description = "Descripción detallada", example = "Planta ornamental de interior perteneciente a la familia Araceae. Es una variedad enana y compacta de Alocasia Amazonica, caracterizada por hojas sagitadas (en forma de flecha) de color verde oscuro metalizado con nervaduras blancas muy pronunciadas y un envés de tono púrpura o burdeos. Su tamaño máximo oscila entre los 30 y 40 cm de altura. Requiere condiciones de alta humedad ambiental (>60%), temperaturas cálidas estables (20°C - 28°C), riego moderado manteniendo el sustrato húmedo pero no encharcado, y exposición a luz brillante indirecta, siendo altamente susceptible a la quema por sol directo y a la pudrición de raíces por exceso de agua.")
    private String descripcion;

    @Schema(description = "Stock disponible", example = "45")
    private Integer stock;
}