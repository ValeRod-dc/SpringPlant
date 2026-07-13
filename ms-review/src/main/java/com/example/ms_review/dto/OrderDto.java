package com.example.ms_review.dto;

import lombok.Data;

@Data
public class OrderDto {
    private Long id;
    private Long clientId;
    private String status;
}
