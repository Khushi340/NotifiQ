package com.khush.notifiq.notification;

public enum NotificationStatus {
    QUEUED,
    SENT,
    FAILED,
    RETRYING,
    DEAD_LETTERED,
    DUPLICATE_IGNORED,
    SKIPPED_BY_PREFERENCE,
    SKIPPED_BY_QUIET_HOURS
}