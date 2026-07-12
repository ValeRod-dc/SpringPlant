package com.example.ms_shipping.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de un envío",
        allowableValues = {"PENDING", "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "CANCELLED"})
public enum ShippingStatus {
    PENDING,
    PREPARING,
    SHIPPED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}