package com.example.ms_cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item para crear una orden desde el carrito")
public class OrderItemDTO {

    @Schema(description = "ID del producto", example = "101")
    private Long productId;

    @Schema(description = "Cantidad del producto", example = "2")
    private Integer quantity;

    @Schema(description = "Precio por unidad", example = "20000")
    private Double unitPrice;

    @Schema(description = "Subtotal (quantity * unitPrice)", example = "40000")
    private Double subtotal;
}