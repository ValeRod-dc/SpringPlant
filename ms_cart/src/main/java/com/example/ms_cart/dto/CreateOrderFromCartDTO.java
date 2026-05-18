package com.example.ms_cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderFromCartDTO {
    private Long userId;
    private List<OrderItemDTO> items;
    private Double totalAmount;
    private String shippingAddress;
    private String couponCode;
}
