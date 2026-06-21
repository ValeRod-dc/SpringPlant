package com.example.notification.model;

import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.model.enums.NotificationStatus;
import com.example.notification.model.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa una notificación enviada a un usuario del sistema")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único de la notificación",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(nullable = false)
    @Schema(
            description = "Identificador del usuario destinatario de la notificación",
            example = "5"
    )
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Tipo de canal por el que se envía la notificación",
            example = "EMAIL",
            allowableValues = {"EMAIL", "SMS"}
    )
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Canal temático al que pertenece la notificación",
            example = "ORDER",
            allowableValues = {"ORDER", "PAYMENT", "SHIPPING", "PROMO", "REVIEW"}
    )
    private NotificationChannel channel;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(
            description = "Contenido del mensaje de la notificación",
            example = "Tu orden #3 ha sido confirmada y está en proceso de envío"
    )
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Estado actual de la notificación",
            example = "PENDING",
            allowableValues = {"PENDING", "SENT", "FAILED"},
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private NotificationStatus status;

    @Schema(
            description = "Fecha y hora en que se envió la notificación",
            example = "2024-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime sentAt;

    @Schema(
            description = "Fecha y hora en que se creó la notificación",
            example = "2024-01-15T10:29:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = NotificationStatus.PENDING;
    }
}