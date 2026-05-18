package com.example.ms_discount.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResult {
    private boolean valid;
    private Double discountAmount;
    private String message;
    private String couponCode;
}