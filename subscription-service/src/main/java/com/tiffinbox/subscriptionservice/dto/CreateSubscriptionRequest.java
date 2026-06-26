package com.tiffinbox.subscriptionservice.dto;

import com.tiffinbox.subscriptionservice.entity.Frequency;
import com.tiffinbox.subscriptionservice.entity.MealType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotNull MealType mealType,
        @NotNull Frequency frequency,
        LocalDate startDate,
        LocalDate endDate
) {
}
