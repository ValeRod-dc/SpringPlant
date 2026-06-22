package com.example.ms_discount.dto.response;

import com.example.ms_discount.model.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Respuesta con datos de un cupón de descuento")
public class DiscountResponseDTO extends RepresentationModel<DiscountResponseDTO> {
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