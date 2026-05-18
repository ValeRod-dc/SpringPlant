package com.example.ms_discount.dto.request;

import com.example.ms_discount.model.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCouponRequest {

    @NotBlank
    private String code;

    private String description;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @Positive
    private Double discountValue;

    @NotNull
    private LocalDateTime validFrom;

    @NotNull
    private LocalDateTime validUntil;

    @PositiveOrZero
    private Integer maxUses = 0; //?

    @PositiveOrZero
    private Double minPurchaseAmount = 0.0;

    private Boolean active = true;

    private String applicableProductIds; //"101, 202, 303"
}
