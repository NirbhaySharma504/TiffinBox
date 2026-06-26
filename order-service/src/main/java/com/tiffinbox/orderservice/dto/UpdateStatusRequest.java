package com.tiffinbox.orderservice.dto;

import com.tiffinbox.orderservice.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull OrderStatus status) {
}
