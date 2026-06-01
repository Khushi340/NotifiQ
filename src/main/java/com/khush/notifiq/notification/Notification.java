package com.khush.notifiq.notification;

import com.khush.notifiq.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String subject;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private int retryCount;

    private LocalDateTime nextRetryAt;

    private String lastError;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void beforeCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
