package com.khush.notifiq.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationStatsResponse {
    private long totalNotifications;

    private long sentNotifications;

    private long queuedNotifications;

    private long retryingNotifications;

    private long failedNotifications;

    private long deadLetteredNotifications;

    private long skippedByPreferenceNotifications;

    private long unreadInAppNotifications;
}
