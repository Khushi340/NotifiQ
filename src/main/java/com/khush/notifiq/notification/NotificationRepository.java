package com.khush.notifiq.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByUserId(Long userId);
    Optional<Notification> findByIdempotencyKey(String idempotencyKey);
    List<Notification> findByStatusInAndNextRetryAtLessThanEqual(
            List<NotificationStatus> statuses,
            LocalDateTime nextRetryAt
    );
    List<Notification> findByUserIdAndChannelAndReadAtIsNull(
            Long userId,
            NotificationChannel channel
    );
    long countByStatus(NotificationStatus status);

    long countByChannelAndReadAtIsNull(NotificationChannel channel);
}
