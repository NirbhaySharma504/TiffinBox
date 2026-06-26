package com.tiffinbox.paymentservice.dto;

import com.tiffinbox.paymentservice.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Internal request from order-service to create a (PENDING) payment for an order.
 */
public record CreatePaymentRequest(
        @NotNull Long orderId,
        @NotNull Long userId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "amount must be positive")
        BigDecimal amount,
        @NotNull PaymentMethod method
) {
}
