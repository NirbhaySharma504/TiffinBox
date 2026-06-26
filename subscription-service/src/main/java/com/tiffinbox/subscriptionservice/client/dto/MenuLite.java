package com.tiffinbox.subscriptionservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Subset of menu-service's MenuResponse needed to build an auto-order. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuLite(
        Long id,
        String mealType,
        String status,
        List<MenuItemLite> items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MenuItemLite(Long id, boolean available) {
    }
}
