package com.tiffinbox.orderservice.client.dto;

import java.util.List;

/** Body sent to menu-service /api/menu/internal/validate-items. */
public record ValidateItemsRequest(Long menuId, List<Long> itemIds) {
}
