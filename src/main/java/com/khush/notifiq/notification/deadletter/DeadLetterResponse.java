package com.khush.notifiq.notification.deadletter;

import com.khush.notifiq.notification.NotificationChannel;
import com.khush.notifiq.notification.NotificationPriority;
import com.khush.notifiq.notification.NotificationStatus;
import com.khush.notifiq.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeadLetterResponse {

    private Long id;
    private Long notificationId;
    private Long userId;

    private NotificationType type;
    private NotificationChannel channel;
    private NotificationPriority priority;

    private String subject;
    private String message;
    private String reason;

    private int retryCount;
    private LocalDateTime failedAt;
}