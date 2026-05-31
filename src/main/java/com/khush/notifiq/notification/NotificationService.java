package com.khush.notifiq.notification;

import com.khush.notifiq.common.ResourceNotFoundException;
import com.khush.notifiq.notification.dto.NotificationRequest;
import com.khush.notifiq.notification.dto.NotificationResponse;
import com.khush.notifiq.preference.UserPreference;
import com.khush.notifiq.preference.UserPreferenceRepository;
import com.khush.notifiq.user.User;
import com.khush.notifiq.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public NotificationResponse createNotification(NotificationRequest request){
        Optional<Notification> existing =notificationRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if(existing.isPresent()){
            return mapToResponse(existing.get());
        }
        User user=userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found with id "+request.getUserId()));

        NotificationStatus status = NotificationStatus.QUEUED;
        Optional<UserPreference> preferenceOptional = userPreferenceRepository.findByUserId(user.getId());
        if(preferenceOptional.isPresent()){
            UserPreference preference = preferenceOptional.get();
            if(request.getChannel() == NotificationChannel.EMAIL && !preference.isEmailEnabled()){
                status = NotificationStatus.SKIPPED_BY_PREFERENCE;
            } else if (request.getChannel() == NotificationChannel.IN_APP && !preference.isInAppEnabled()) {
                status = NotificationStatus.SKIPPED_BY_PREFERENCE;
            } else if (request.getChannel() == NotificationChannel.WEBHOOK && !preference.isWebhookEnabled()) {
                status = NotificationStatus.SKIPPED_BY_PREFERENCE;
            }
        }


        Notification notification= Notification.builder()
                .user(user)
                .status(status)
                .type(request.getType())
                .channel(request.getChannel())
                .priority(request.getPriority())
                .subject(request.getSubject())
                .message(request.getMessage())
                .idempotencyKey(request.getIdempotencyKey())
                .build();
        Notification savedNotification = notificationRepository.save(notification);
        return mapToResponse(savedNotification);
    }

    public List<NotificationResponse> getNotificationsByUserId(Long userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private NotificationResponse mapToResponse(Notification notification){
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .priority(notification.getPriority())
                .status(notification.getStatus())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .idempotencyKey(notification.getIdempotencyKey())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }

}
