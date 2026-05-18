package com.example.notification.dto;

import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequestDTO {

    @NotNull(message = "El userId es obligatorio")
    private Long userId;

    @NotNull(message = "El tipo es obligatorio")
    private NotificationType type;

    @NotNull(message = "El canal es obligatorio")
    private NotificationChannel channel;

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;
}
