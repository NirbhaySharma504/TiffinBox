package com.tiffinbox.menuservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Internal (service-to-service) request used by order-service before placing an order:
 * "are these items valid for this menu, and what are their authoritative name/price?"
 */
public record ValidateItemsRequest(
        @NotNull Long menuId,
        @NotEmpty List<Long> itemIds
) {
}
