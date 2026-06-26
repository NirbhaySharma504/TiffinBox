package com.tiffinbox.menuservice.dto;

import com.tiffinbox.menuservice.entity.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreateMenuRequest(
        @NotNull LocalDate date,
        @NotNull MealType mealType,
        String description,
        @NotNull LocalTime cutoffTime,
        @NotEmpty(message = "a menu must have at least one item")
        @Valid List<CreateMenuItemRequest> items
) {
}
