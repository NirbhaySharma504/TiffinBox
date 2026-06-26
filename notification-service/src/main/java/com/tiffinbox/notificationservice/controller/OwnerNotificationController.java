package com.tiffinbox.notificationservice.controller;

import com.tiffinbox.notificationservice.dto.NotificationResponse;
import com.tiffinbox.notificationservice.exception.ForbiddenException;
import com.tiffinbox.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications/owner")
@RequiredArgsConstructor
public class OwnerNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> all(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"OWNER".equals(role)) {
            throw new ForbiddenException("Only the owner can view all notifications");
        }
        return ResponseEntity.ok(notificationService.getAll().stream()
                .map(NotificationResponse::from).toList());
    }
}
