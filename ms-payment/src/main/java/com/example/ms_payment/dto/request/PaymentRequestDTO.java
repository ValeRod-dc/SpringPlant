package com.example.ms_payment.dto.request;

import com.example.ms_payment.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Solicitud para procesar un pago")
public class PaymentRequestDTO {

    @NotNull(message = "El orderId es obligatorio")
    @Schema(description = "ID de la orden a pagar", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotNull(message = "El amount es obligatorio")
    @Positive(message = "El amount debe ser mayor a cero")
    @Schema(description = "Monto a pagar", example = "45990.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double amount;

    @NotNull(message = "El método de pago es obligatorio")
    @Schema(description = "Método de pago", example = "CREDIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentMethod method;
}

