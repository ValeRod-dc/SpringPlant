package com.example.ms_cart.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CartResponseDTO {
    private Long cartId;
    private Long userId;
    private List<CartItemResponseDTO> items;
    private Double total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}