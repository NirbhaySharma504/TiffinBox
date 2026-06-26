package com.tiffinbox.orderservice.dto;

import com.tiffinbox.orderservice.entity.Order;
import com.tiffinbox.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        Long menuId,
        OrderStatus status,
        BigDecimal totalAmount,
        Long paymentId,
        List<OrderItemResponse> items,
        Instant createdAt
) {
    public static OrderResponse from(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(OrderItemResponse::from).toList();
        return new OrderResponse(o.getId(), o.getUserId(), o.getMenuId(), o.getStatus(),
                o.getTotalAmount(), o.getPaymentId(), items, o.getCreatedAt());
    }
}
