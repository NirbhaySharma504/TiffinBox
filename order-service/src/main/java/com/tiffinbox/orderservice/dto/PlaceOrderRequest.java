package com.tiffinbox.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaceOrderRequest(
        @NotNull Long menuId,
        @NotEmpty(message = "an order must have at least one item")
        @Valid List<OrderItemRequest> items
) {
}
