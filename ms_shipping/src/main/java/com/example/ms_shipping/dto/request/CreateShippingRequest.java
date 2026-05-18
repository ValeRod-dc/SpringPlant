package com.example.ms_shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateShippingRequest {

    @NotNull(message = "El orderId es obligatorio")
    private Long orderId;

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String address;
}