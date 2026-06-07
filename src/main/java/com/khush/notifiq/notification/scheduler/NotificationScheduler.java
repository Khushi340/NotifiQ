package com.khush.notifiq.notification.scheduler;

import com.khush.notifiq.notification.Notification;
import com.khush.notifiq.notification.NotificationRepository;
import com.khush.notifiq.notification.NotificationStatus;
import com.khush.notifiq.notification.delivery.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatcher notificationDispatcher;

    @Scheduled(fixedDelay = 60000)
    public void processQueuedNotifications() {
        List<Notification> notifications = notificationRepository.findByStatusInAndNextRetryAtLessThanEqual(
                List.of(
                        NotificationStatus.QUEUED,
                        NotificationStatus.RETRYING
                ),
                LocalDateTime.now()
        );

        for (Notification notification : notifications) {
            notification.setNextRetryAt(null);
            notificationRepository.save(notification);

            notificationDispatcher.dispatch(notification);
        }
    }
}