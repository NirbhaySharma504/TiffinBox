package com.tiffinbox.paymentservice.dto;

import java.math.BigDecimal;

/** Owner dashboard summary of payments. */
public record PaymentSummaryResponse(
        BigDecimal totalCollected,
        long paidCount,
        long pendingCount,
        long failedCount
) {
}
