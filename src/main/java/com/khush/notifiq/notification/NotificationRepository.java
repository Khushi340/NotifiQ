package com.khush.notifiq.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByUserId(Long userId);
    Optional<Notification> findByIdempotencyKey(String idempotencyKey);
}
