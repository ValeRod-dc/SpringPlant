package com.example.ms_notification.dto;
import com.example.ms_notification.model.enums.NotificationChannel;
import com.example.ms_notification.model.enums.NotificationStatus;
import com.example.ms_notification.model.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {

    private Long id;
    private Long userId;
    private NotificationType type;
    private NotificationChannel channel;
    private String message;
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
