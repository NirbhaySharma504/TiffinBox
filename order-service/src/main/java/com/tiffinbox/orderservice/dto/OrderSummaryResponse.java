package com.tiffinbox.orderservice.dto;

import java.math.BigDecimal;

public record OrderSummaryResponse(
        long placed,
        long preparing,
        long outForDelivery,
        long delivered,
        long cancelled,
        BigDecimal totalRevenue
) {
}
