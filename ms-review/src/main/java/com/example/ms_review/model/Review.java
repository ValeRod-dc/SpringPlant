package com.example.ms_review.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa una reseña de un producto realizada por un usuario")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único de la reseña",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(nullable = false)
    @Schema(
            description = "Identificador del usuario que realizó la reseña",
            example = "5"
    )
    private Long userId;

    @Column(nullable = false)
    @Schema(
            description = "Identificador del producto reseñado",
            example = "10"
    )
    private Long productId;

    @Column(nullable = false)
    @Schema(
            description = "Identificador de la orden asociada a la reseña",
            example = "3"
    )
    private Long orderId;

    @Column(nullable = false)
    @Schema(
            description = "Puntuación otorgada al producto, entre 1 y 5",
            example = "4",
            minimum = "1",
            maximum = "5"
    )
    private Integer rating;

    @Column(length = 500)
    @Schema(
            description = "Comentario opcional del usuario sobre el producto",
            example = "Excelente planta, llegó en perfectas condiciones",
            maxLength = 500
    )
    private String comment;

    @Schema(
            description = "Fecha y hora en que se creó la reseña",
            example = "2024-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}