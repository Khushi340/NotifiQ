package com.khush.notifiq.notification;

import com.khush.notifiq.common.ResourceNotFoundException;
import com.khush.notifiq.notification.dto.NotificationRequest;
import com.khush.notifiq.notification.dto.NotificationResponse;
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

    public NotificationResponse createNotification(NotificationRequest request){
        Optional<Notification> existing =notificationRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if(existing.isPresent()){
            return mapToResponse(existing.get());
        }
        User user=userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found with id "+request.getUserId()));
        Notification notification= Notification.builder()
                .user(user)
                .status(NotificationStatus.QUEUED)
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
