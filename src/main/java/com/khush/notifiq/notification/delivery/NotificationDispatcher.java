package com.khush.notifiq.notification.delivery;

import com.khush.notifiq.common.BadRequestException;
import com.khush.notifiq.notification.Notification;
import com.khush.notifiq.notification.NotificationChannel;
import com.khush.notifiq.notification.NotificationRepository;
import com.khush.notifiq.notification.NotificationStatus;
import com.khush.notifiq.notification.deadletter.DeadLetterService;
import com.khush.notifiq.preference.UserPreference;
import com.khush.notifiq.preference.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final EmailNotificationSender emailNotificationSender;
    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final DeadLetterService deadLetterService;
    private final WebhookNotificationSender webhookNotificationSender;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Async("notificationTaskExecutor")
    public void dispatch(Notification notification) {

        if (notification.getChannel() == NotificationChannel.EMAIL) {
            sendEmail(notification);
        } else if (notification.getChannel() == NotificationChannel.IN_APP) {
            sendInApp(notification);
        } else if (notification.getChannel() == NotificationChannel.WEBHOOK) {
            sendWebhook(notification);
        }
    }

    private void sendEmail(Notification notification) {
        try {
            String subject = notification.getSubject() != null
                    ? notification.getSubject()
                    : "NotifiQ Notification";

            emailNotificationSender.sendEmail(
                    notification.getUser().getEmail(),
                    subject,
                    notification.getMessage()
            );

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setLastError(null);
            notification.setNextRetryAt(null);

        } catch (Exception ex) {
            handleFailure(notification, ex);
        }

        notificationRepository.save(notification);
    }

    private void sendInApp(Notification notification) {
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notification.setLastError(null);
        notification.setNextRetryAt(null);

        notificationRepository.save(notification);
    }

    private void sendWebhook(Notification notification) {
        try {
            UserPreference preference = userPreferenceRepository
                    .findByUserId(notification.getUser().getId())
                    .orElseThrow(() -> new BadRequestException(
                            "Webhook preference not found for user id " + notification.getUser().getId()
                    ));

            String webhookUrl = preference.getWebhookUrl();

            if (webhookUrl == null || webhookUrl.isBlank()) {
                throw new BadRequestException("Webhook URL is missing for user id " + notification.getUser().getId());
            }

            webhookNotificationSender.sendWebhook(webhookUrl, notification);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setLastError(null);
            notification.setNextRetryAt(null);

        } catch (Exception ex) {
            handleFailure(notification, ex);
        }

        notificationRepository.save(notification);
    }

    private void handleFailure(Notification notification, Exception ex) {
        int nextRetryCount = notification.getRetryCount() + 1;

        notification.setRetryCount(nextRetryCount);
        notification.setLastError(ex.getMessage());

        if (nextRetryCount > MAX_RETRY_ATTEMPTS) {
            notification.setStatus(NotificationStatus.DEAD_LETTERED);
            notification.setNextRetryAt(null);

            deadLetterService.createDeadLetter(notification, ex.getMessage());
        } else {
            notification.setStatus(NotificationStatus.RETRYING);
            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(nextRetryCount));
        }
    }
}