package com.tiffinbox.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Shape of the {@code order-placed} event published by order-service to the
 * {@code order-events} topic. Defined independently here (no shared module) and
 * tolerant of extra/missing fields so the two services stay loosely coupled.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderPlacedEvent(
        Long orderId,
        Long userId,
        String customerEmail,
        String customerName,
        BigDecimal totalAmount,
        Instant placedAt
) {
}
