package com.tiffinbox.orderservice.dto;

import com.tiffinbox.orderservice.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long menuItemId,
        String itemName,
        BigDecimal price,
        int quantity,
        BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem i) {
        return new OrderItemResponse(i.getMenuItemId(), i.getItemName(), i.getPrice(),
                i.getQuantity(), i.getSubtotal());
    }
}
