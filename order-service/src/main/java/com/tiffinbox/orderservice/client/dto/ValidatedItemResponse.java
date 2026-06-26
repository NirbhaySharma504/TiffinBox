package com.tiffinbox.orderservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Authoritative item snapshot returned by menu-service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ValidatedItemResponse(Long itemId, String name, BigDecimal price) {
}
