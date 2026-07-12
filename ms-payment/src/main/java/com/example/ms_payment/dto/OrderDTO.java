package com.example.ms_payment.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long Id;
    private Long clientId;
    private List<OrderItemDTO> items;
    private Double totalAmount;
    private String status;
    private String paymentStatus;
    private String shippingAddress;
    private LocalDateTime createdAt;
}