package com.khush.notifiq.notification.deadletter;

import com.khush.notifiq.common.ResourceNotFoundException;
import com.khush.notifiq.notification.Notification;
import com.khush.notifiq.notification.NotificationRepository;
import com.khush.notifiq.notification.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeadLetterService {
    private final DeadLetterNotificationRepository deadLetterNotificationRepository;
    private final NotificationRepository notificationRepository;

    public void createDeadLetter(Notification notification, String reason){
        if (deadLetterNotificationRepository.existsByNotificationId(notification.getId())) {
            return;
        }

        DeadLetterNotification deadLetterNotification=DeadLetterNotification.builder()
                        .notification(notification)
                        .reason(reason)
                        .failedAt(LocalDateTime.now())
                        .build();

        deadLetterNotificationRepository.save(deadLetterNotification);
    }

    public List<DeadLetterResponse> getAllDeadLetters(){
        return deadLetterNotificationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DeadLetterResponse replayDeadLetter(Long deadLetterId) {
        DeadLetterNotification deadLetter = deadLetterNotificationRepository.findById(deadLetterId)
                .orElseThrow(() -> new ResourceNotFoundException("Dead letter not found with id " + deadLetterId));

        Notification notification = deadLetter.getNotification();

        notification.setStatus(NotificationStatus.QUEUED);
        notification.setRetryCount(0);
        notification.setLastError(null);
        notification.setNextRetryAt(LocalDateTime.now());
        notification.setSentAt(null);

        DeadLetterResponse response = mapToResponse(deadLetter);
        notificationRepository.save(notification);
        deadLetterNotificationRepository.delete(deadLetter);

        return response;
    }

    private DeadLetterResponse mapToResponse(DeadLetterNotification deadLetter){
        Notification notification=deadLetter.getNotification();
        return DeadLetterResponse.builder()
                        .id(deadLetter.getId())
                        .notificationId(notification.getId())
                        .userId(notification.getUser().getId())
                        .type(notification.getType())
                        .channel(notification.getChannel())
                        .priority(notification.getPriority())
                        .subject(notification.getSubject())
                        .message(notification.getMessage())
                        .reason(deadLetter.getReason())
                        .retryCount(notification.getRetryCount())
                        .failedAt(deadLetter.getFailedAt())
                        .build();
    }
}
