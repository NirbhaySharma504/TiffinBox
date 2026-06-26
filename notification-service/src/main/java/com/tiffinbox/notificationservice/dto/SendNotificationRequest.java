package com.tiffinbox.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Manual REST trigger to send an ad-hoc notification (useful for testing/demo). */
public record SendNotificationRequest(
        @NotBlank @Email String recipientEmail,
        @NotBlank String subject,
        @NotBlank String body,
        Long orderId,
        Long userId
) {
}
