package com.tiffinbox.subscriptionservice.dto;

import com.tiffinbox.subscriptionservice.entity.Frequency;
import com.tiffinbox.subscriptionservice.entity.MealType;
import com.tiffinbox.subscriptionservice.entity.Subscription;
import com.tiffinbox.subscriptionservice.entity.SubscriptionStatus;

import java.time.Instant;
import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        Long userId,
        String customerEmail,
        MealType mealType,
        Frequency frequency,
        SubscriptionStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate lastOrderedOn,
        Instant createdAt
) {
    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(s.getId(), s.getUserId(), s.getCustomerEmail(),
                s.getMealType(), s.getFrequency(), s.getStatus(), s.getStartDate(),
                s.getEndDate(), s.getLastOrderedOn(), s.getCreatedAt());
    }
}
