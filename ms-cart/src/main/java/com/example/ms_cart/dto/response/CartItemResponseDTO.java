package com.example.ms_cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Item del carrito en la respuesta")
public class CartItemResponseDTO {

    @Schema(description = "ID del producto", example = "101")
    private Long productId;

    @Schema(description = "Nombre del producto (obtenido del ms-products", example = "Monstera Thai Constellation")
    private String productName;

    @Schema(description = "Cantidad seleccionada", example = "2")
    private Integer quantity;

    @Schema(description = "Precio unitario al momento de agregar", example = "30000")
    private Double unitPrice;

    @Schema(description = "Subtotal (quantity * unitPrice)", example = "60000")
    private Double subtotal;
}