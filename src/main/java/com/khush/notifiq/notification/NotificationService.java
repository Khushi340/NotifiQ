package com.khush.notifiq.notification;

import com.khush.notifiq.common.BadRequestException;
import com.khush.notifiq.common.ResourceNotFoundException;
import com.khush.notifiq.notification.delivery.NotificationDispatcher;
import com.khush.notifiq.notification.dto.NotificationRequest;
import com.khush.notifiq.notification.dto.NotificationResponse;
import com.khush.notifiq.notification.dto.NotificationStatsResponse;
import com.khush.notifiq.preference.UserPreference;
import com.khush.notifiq.preference.UserPreferenceRepository;
import com.khush.notifiq.user.User;
import com.khush.notifiq.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final NotificationDispatcher notificationDispatcher;

    public NotificationResponse createNotification(NotificationRequest request){
        Optional<Notification> existing =notificationRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if(existing.isPresent()){
            return mapToResponse(existing.get());
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found with id "+request.getUserId()));

        NotificationStatus status = NotificationStatus.QUEUED;
        LocalDateTime nextRetryAt = null;
        boolean shouldDeliverNow = true;

        Optional<UserPreference> preferenceOptional = userPreferenceRepository.findByUserId(user.getId());

        if(preferenceOptional.isPresent()){
            UserPreference preference = preferenceOptional.get();
            if(request.getChannel() == NotificationChannel.EMAIL && !preference.isEmailEnabled()){
                status = NotificationStatus.SKIPPED_BY_PREFERENCE;
                shouldDeliverNow = false;
            } else if (request.getChannel() == NotificationChannel.IN_APP && !preference.isInAppEnabled()) {
                status = NotificationStatus.SKIPPED_BY_PREFERENCE;
                shouldDeliverNow = false;
            } else if (request.getChannel() == NotificationChannel.WEBHOOK && !preference.isWebhookEnabled()) {
                status = NotificationStatus.SKIPPED_BY_PREFERENCE;
                shouldDeliverNow = false;
            }

            if (shouldDeliverNow
                    && preference.isQuietHoursEnabled()
                    && isWithinQuietHours(
                    LocalTime.now(),
                    preference.getQuietHoursStart(),
                    preference.getQuietHoursEnd())) {

                nextRetryAt = calculateNextRetryAt(preference.getQuietHoursEnd());
                shouldDeliverNow = false;
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
                .nextRetryAt(nextRetryAt)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        if (shouldDeliverNow && savedNotification.getStatus() == NotificationStatus.QUEUED) {
            notificationDispatcher.dispatch(savedNotification);
        }

        return mapToResponse(savedNotification);
    }

    public List<NotificationResponse> getNotificationsByUserId(Long userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return notificationRepository.findByUserIdOrderByCreatedAt(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public NotificationResponse markAsRead(Long notificationId){
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Notification not found with id "+notificationId
                ));
        if(notification.getChannel() != NotificationChannel.IN_APP){
            throw new BadRequestException("Only IN_APP notifications can be marked as read");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId){
        userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id "+ userId));
        return notificationRepository
                .findByUserIdAndChannelAndReadAtIsNull(userId,NotificationChannel.IN_APP)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public NotificationStatsResponse getNotificationStats(){
        return NotificationStatsResponse.builder()
                .totalNotifications(notificationRepository.count())
                .sentNotifications(notificationRepository.countByStatus(NotificationStatus.SENT))
                .queuedNotifications(notificationRepository.countByStatus(NotificationStatus.QUEUED))
                .retryingNotifications(notificationRepository.countByStatus(NotificationStatus.RETRYING))
                .failedNotifications(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .deadLetteredNotifications(notificationRepository.countByStatus(NotificationStatus.DEAD_LETTERED))
                .skippedByPreferenceNotifications(notificationRepository.countByStatus(NotificationStatus.SKIPPED_BY_PREFERENCE))
                .unreadInAppNotifications(
                        notificationRepository.countByChannelAndReadAtIsNull(NotificationChannel.IN_APP)
                )
                .build();
    }

    public List<NotificationResponse> getRecentNotifications() {
        return notificationRepository
                .findTop10ByOrderByCreatedAtDesc()
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
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .nextRetryAt(notification.getNextRetryAt())
                .readAt(notification.getReadAt())
                .build();
    }

    private boolean isWithinQuietHours(LocalTime now, LocalTime start, LocalTime end) {
        if (now == null || start == null || end == null || start.equals(end)) {
            return false;
        }

        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }

        return !now.isBefore(start) || now.isBefore(end);
    }

    private LocalDateTime calculateNextRetryAt(LocalTime quietHoursEnd) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRetryAt = now.toLocalDate().atTime(quietHoursEnd);

        if (!nextRetryAt.isAfter(now)) {
            nextRetryAt = nextRetryAt.plusDays(1);
        }
        return nextRetryAt;
    }
}
