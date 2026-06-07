package com.khush.notifiq.notification.deadletter;

import com.khush.notifiq.notification.Notification;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dead_letter_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "notification_id", nullable = false, unique = true)
    private Notification notification;

    private String reason;

    private LocalDateTime failedAt;
}