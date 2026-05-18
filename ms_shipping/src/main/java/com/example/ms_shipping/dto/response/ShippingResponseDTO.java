package com.example.ms_shipping.dto.response;

import com.example.ms_shipping.model.ShippingStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ShippingResponseDTO {
    private Long shippingId;
    private Long orderId;
    private Long userId;
    private String address;
    private ShippingStatus status;
    private String trackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime deliveredAt;
}