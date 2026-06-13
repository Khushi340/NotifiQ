package com.khush.notifiq.preference.dto;

import com.khush.notifiq.notification.NotificationChannel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class PreferenceRequest {
    private boolean emailEnabled;
    private boolean inAppEnabled;
    private boolean webhookEnabled;
    private String webhookUrl;
    private boolean quietHoursEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;

    @NotNull(message = "Preferred Channel is required")
    private NotificationChannel preferredChannel;
}
