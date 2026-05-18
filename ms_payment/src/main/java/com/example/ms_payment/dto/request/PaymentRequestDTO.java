package com.example.ms_payment.dto.request;

import com.example.ms_payment.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequestDTO {

    @NotNull(message = "El orderId es obligatorio")
    private Long orderId;

    @NotNull(message = "El amount es obligatorio")
    @Positive(message = "El amount debe ser mayor a cero")
    private Double amount;

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod method;
}
