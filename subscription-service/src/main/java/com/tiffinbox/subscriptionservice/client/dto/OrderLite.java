package com.tiffinbox.subscriptionservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Subset of order-service's OrderResponse we care about (the new order id). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderLite(Long id) {
}
