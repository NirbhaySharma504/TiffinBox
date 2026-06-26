package com.tiffinbox.paymentservice.dto;

import com.tiffinbox.paymentservice.entity.Payment;
import com.tiffinbox.paymentservice.entity.PaymentMethod;
import com.tiffinbox.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String transactionRef,
        Instant createdAt,
        Instant paidAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getOrderId(), p.getUserId(), p.getAmount(),
                p.getMethod(), p.getStatus(), p.getTransactionRef(),
                p.getCreatedAt(), p.getPaidAt()
        );
    }
}
