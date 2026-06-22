package com.example.ms_discount.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResult extends RepresentationModel<DiscountResult> {
    private boolean valid;
    private Double discountAmount;
    private String message;
    private String couponCode;
}