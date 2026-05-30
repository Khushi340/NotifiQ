package com.khush.notifiq.notification;

import com.khush.notifiq.notification.dto.NotificationRequest;
import com.khush.notifiq.notification.dto.NotificationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody @Valid NotificationRequest notificationRequest){
        NotificationResponse savedResponse= notificationService.createNotification(notificationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@PathVariable Long userId){
        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

}
