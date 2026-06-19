package com.example.ms_cart.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado del carrito",
        allowableValues = {"ACTIVE", "CHECKED_OUT", "ABANDONED"})
public enum CartStatus {
    ACTIVE,
    CHECKED_OUT,
    ABANDONED
}