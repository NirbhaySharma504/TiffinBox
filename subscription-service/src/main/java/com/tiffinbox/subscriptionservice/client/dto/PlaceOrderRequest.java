package com.tiffinbox.subscriptionservice.client.dto;

import java.util.List;

/** Body sent to order-service POST /api/orders. */
public record PlaceOrderRequest(Long menuId, List<OrderLine> items) {

    public record OrderLine(Long menuItemId, int quantity) {
    }
}
