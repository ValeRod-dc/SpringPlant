package com.example.ms_payment.dto.response;

import com.example.ms_payment.model.PaymentMethod;
import com.example.ms_payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Respuesta con datos de un pago")
public class PaymentResponseDTO extends RepresentationModel<PaymentResponseDTO> {

    @Schema(description = "ID del pago", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long paymentId;

    @Schema(description = "ID de la orden asociada", example = "101")
    private Long orderId;

    @Schema(description = "ID del usuario que realizó el pago", example = "5")
    private Long userId;

    @Schema(description = "Monto pagado", example = "45990.0")
    private Double amount;

    @Schema(description = "Método de pago utilizado", example = "CREDIT_CARD")
    private PaymentMethod method;

    @Schema(description = "Estado del pago", example = "COMPLETED")
    private PaymentStatus status;

    @Schema(description = "ID de la transacción generado por la pasarela de pago", example = "txn_8f3a1c2d")
    private String transactionId;

    @Schema(description = "Fecha de creación del pago", example = "2026-01-15T12:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de finalización del pago", example = "2026-01-15T12:31:05", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime completedAt;

    @Schema(description = "Mensaje de error en caso de pago fallido", example = "Tarjeta rechazada por el banco emisor")
    private String errorMessage;
}