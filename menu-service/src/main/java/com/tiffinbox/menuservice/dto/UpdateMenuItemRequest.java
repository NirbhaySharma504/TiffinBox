package com.tiffinbox.menuservice.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/** Partial update for an item: any null field is left unchanged. */
public record UpdateMenuItemRequest(
        @DecimalMin(value = "0.0", inclusive = false, message = "price must be positive")
        BigDecimal price,
        Boolean available
) {
}
