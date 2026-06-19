package com.example.review.dto;

import lombok.Data;

@Data
public class OrderDto {
    private Long id;
    private Long clientId;
    private String status;
}
