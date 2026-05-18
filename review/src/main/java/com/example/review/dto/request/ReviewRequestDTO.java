package com.example.review.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequestDTO {

    @NotNull(message = "El userId es obligatorio")
    private Long userId;

    @NotNull(message = "El productId es obligatorio")
    private Long productId;

    @NotNull(message = "El orderId es obligatorio")
    private Long orderId;

    @NotNull(message = "El rating es obligatorio")
    @Min(value = 1, message = "El rating mínimo es 1")
    @Max(value = 5, message = "El rating máximo es 5")
    private Integer rating;

    @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
    private String comment;
}