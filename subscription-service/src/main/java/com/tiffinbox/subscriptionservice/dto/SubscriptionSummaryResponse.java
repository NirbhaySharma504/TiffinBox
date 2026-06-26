package com.tiffinbox.subscriptionservice.dto;

public record SubscriptionSummaryResponse(long active, long paused, long cancelled) {
}
