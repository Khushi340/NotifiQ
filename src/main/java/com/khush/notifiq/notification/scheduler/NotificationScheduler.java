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
        List<Notification> notifications = notificationRepository
                .findByStatusAndNextRetryAtLessThanEqual(
                        NotificationStatus.QUEUED,
                        LocalDateTime.now()
                );

        notifications.forEach(notificationDispatcher::dispatch);
    }
}