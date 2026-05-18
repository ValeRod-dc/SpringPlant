package com.example.ms_payment.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDTO {
    private Long cartId;
    private Long userId;
    private List<CartItemDTO> items;
    private Double total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
