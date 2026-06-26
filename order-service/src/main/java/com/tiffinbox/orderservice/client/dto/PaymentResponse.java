package com.tiffinbox.orderservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Subset of payment-service's response we care about (the payment id). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentResponse(Long id) {
}
