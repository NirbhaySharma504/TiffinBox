package com.tiffinbox.orderservice.client.dto;

import java.math.BigDecimal;

/** Body sent to payment-service /api/payments/internal. method is a plain string (e.g. "UPI"). */
public record CreatePaymentRequest(Long orderId, Long userId, BigDecimal amount, String method) {
}
