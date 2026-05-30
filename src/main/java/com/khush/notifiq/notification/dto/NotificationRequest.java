package com.khush.notifiq.notification.dto;

import com.khush.notifiq.notification.NotificationChannel;
import com.khush.notifiq.notification.NotificationPriority;
import com.khush.notifiq.notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Priority is required")
    private NotificationPriority priority;

    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
