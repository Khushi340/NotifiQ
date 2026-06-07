package com.khush.notifiq.notification.deadletter;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterNotificationRepository extends JpaRepository<DeadLetterNotification, Long> {
    boolean existsByNotificationId(Long notificationId);
}
