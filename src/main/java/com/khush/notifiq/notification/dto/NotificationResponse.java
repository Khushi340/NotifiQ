package com.khush.notifiq.notification.dto;

import com.khush.notifiq.notification.NotificationChannel;
import com.khush.notifiq.notification.NotificationPriority;
import com.khush.notifiq.notification.NotificationStatus;
import com.khush.notifiq.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationPriority priority;
    private NotificationStatus status;
    private String subject;
    private String message;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
