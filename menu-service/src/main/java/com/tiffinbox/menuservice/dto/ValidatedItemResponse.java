package com.tiffinbox.menuservice.dto;

import com.tiffinbox.menuservice.entity.MenuItem;

import java.math.BigDecimal;

/**
 * Authoritative item snapshot returned to order-service so it can denormalize
 * name + price into its order_items (price at time of order).
 */
public record ValidatedItemResponse(
        Long itemId,
        String name,
        BigDecimal price
) {
    public static ValidatedItemResponse from(MenuItem item) {
        return new ValidatedItemResponse(item.getId(), item.getName(), item.getPrice());
    }
}
