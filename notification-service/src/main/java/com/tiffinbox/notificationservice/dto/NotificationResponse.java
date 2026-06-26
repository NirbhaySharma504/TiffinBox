package com.tiffinbox.notificationservice.dto;

import com.tiffinbox.notificationservice.entity.Notification;
import com.tiffinbox.notificationservice.entity.NotificationStatus;
import com.tiffinbox.notificationservice.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long orderId,
        Long userId,
        NotificationType type,
        String recipientEmail,
        String subject,
        NotificationStatus status,
        String failureReason,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getOrderId(), n.getUserId(), n.getType(),
                n.getRecipientEmail(), n.getSubject(), n.getStatus(),
                n.getFailureReason(), n.getCreatedAt()
        );
    }
}
