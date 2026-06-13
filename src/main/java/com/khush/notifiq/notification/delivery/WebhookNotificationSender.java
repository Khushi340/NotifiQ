package com.khush.notifiq.notification.delivery;

import com.khush.notifiq.notification.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebhookNotificationSender {

    private final RestTemplate restTemplate;

    public void sendWebhook(String webhookUrl, Notification notification) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("notificationId", notification.getId());
        payload.put("userId", notification.getUser().getId());
        payload.put("type", notification.getType());
        payload.put("channel", notification.getChannel());
        payload.put("priority", notification.getPriority());
        payload.put("subject", notification.getSubject());
        payload.put("message", notification.getMessage());
        payload.put(
                "createdAt",
                notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null
        );

        restTemplate.postForEntity(webhookUrl, payload, String.class);
    }
}