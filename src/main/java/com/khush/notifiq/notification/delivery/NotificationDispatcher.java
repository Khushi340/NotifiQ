package com.khush.notifiq.notification.delivery;

import com.khush.notifiq.notification.Notification;
import com.khush.notifiq.notification.NotificationChannel;
import com.khush.notifiq.notification.NotificationRepository;
import com.khush.notifiq.notification.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final EmailNotificationSender emailNotificationSender;
    private final NotificationRepository notificationRepository;

    public Notification dispatch(Notification notification) {
        if (notification.getChannel() == NotificationChannel.EMAIL) {
            return sendEmail(notification);
        }
        return notification;
    }

    private Notification sendEmail(Notification notification) {
        try {
            String subject = notification.getSubject() != null
                    ? notification.getSubject()
                    : "NotifiQ Notification";

            emailNotificationSender.sendEmail(
                    notification.getUser().getEmail(),
                    subject,
                    notification.getMessage()
            );

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setLastError(null);
            notification.setNextRetryAt(null);

        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setLastError(ex.getMessage());
        }

        return notificationRepository.save(notification);
    }
}