package com.example.ms_discount.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ValidateCouponRequest {
    @NotBlank
    private String code;

    private Long userId; //para limitar x usuario

    @PositiveOrZero
    private Double cartTotal = 0.0;
}
