package com.khush.notifiq.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationStatsResponse {
    private long totalNotifications;

    private long sentNotifications;

    private long queuedNotifications;

    private long failedNotifications;

    private long unreadInAppNotifications;
}
