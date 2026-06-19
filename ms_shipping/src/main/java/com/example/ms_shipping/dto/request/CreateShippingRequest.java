package com.example.ms_shipping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Solicitud para crear un envío")
public class CreateShippingRequest {

    @NotNull(message = "El orderId es obligatorio")
    @Schema(description = "ID de la orden",
            example = "1001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotBlank(message = "La dirección de envío es obligatoria")
    @Schema(description = "Dirección completa de envío",
            example = "Av. Siempre Viva 123, Santiago",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;
}