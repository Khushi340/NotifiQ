package com.khush.notifiq.preference.dto;

import com.khush.notifiq.notification.NotificationChannel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class PreferenceResponse {
    private Long id;
    private Long userId;
    private boolean emailEnabled;
    private boolean inAppEnabled;
    private boolean webhookEnabled;
    private boolean quietHoursEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private NotificationChannel preferredChannel;
}
