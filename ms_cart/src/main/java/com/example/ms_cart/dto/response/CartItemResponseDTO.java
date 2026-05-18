package com.example.ms_cart.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponseDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
}