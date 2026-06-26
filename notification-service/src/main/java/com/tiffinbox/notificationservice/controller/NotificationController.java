package com.tiffinbox.notificationservice.controller;

import com.tiffinbox.notificationservice.dto.NotificationResponse;
import com.tiffinbox.notificationservice.dto.SendNotificationRequest;
import com.tiffinbox.notificationservice.service.NotificationService;
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

    /** Manual REST trigger to send an ad-hoc notification (testing/demo). */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationResponse.from(notificationService.sendManual(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> myNotifications(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getByUser(userId).stream()
                .map(NotificationResponse::from).toList());
    }
}
