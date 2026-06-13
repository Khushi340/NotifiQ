package com.khush.notifiq.preference;

import com.khush.notifiq.notification.NotificationChannel;
import com.khush.notifiq.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false,unique = true)
    private User user;
    private boolean emailEnabled;
    private boolean inAppEnabled;
    private boolean webhookEnabled;
    private String webhookUrl;
    private boolean quietHoursEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    @Enumerated(EnumType.STRING)
    private NotificationChannel preferredChannel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforeCreate(){
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    public void beforeUpdate(){
        updatedAt=LocalDateTime.now();
    }
}
