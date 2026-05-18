package com.example.ms_payment.dto.response;

import com.example.ms_payment.model.PaymentMethod;
import com.example.ms_payment.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDTO {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
}