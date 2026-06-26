package com.tiffinbox.orderservice.kafka;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published to the {@code order-events} topic when an order is placed.
 * notification-service consumes this (matching shape) to email the customer.
 */
public record OrderPlacedEvent(
        Long orderId,
        Long userId,
        String customerEmail,
        BigDecimal totalAmount,
        Instant placedAt
) {
}
