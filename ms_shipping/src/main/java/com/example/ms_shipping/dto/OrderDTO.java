package com.example.ms_shipping.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long clientId;
    private String status;
    private String paymentStatus;
    private String shippingAddress;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}

@Data
class OrderItemDTO {
    private Long productId;
    private Integer quantity;
    private Double price;
}