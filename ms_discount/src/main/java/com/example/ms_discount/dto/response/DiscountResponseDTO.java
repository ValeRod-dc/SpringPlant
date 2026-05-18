package com.example.ms_discount.dto.response;

import com.example.ms_discount.model.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DiscountResponseDTO {
    private Long discountId;
    private String code;
    private String description;
    private DiscountType discountType;
    private Double discountValue;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer maxUses;
    private Integer currentUses;
    private Double minPurchaseAmount;
    private Boolean active;
    private String applicableProductIds;
}