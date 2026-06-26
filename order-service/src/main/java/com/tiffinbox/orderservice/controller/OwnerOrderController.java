package com.tiffinbox.orderservice.controller;

import com.tiffinbox.orderservice.dto.OrderResponse;
import com.tiffinbox.orderservice.dto.OrderSummaryResponse;
import com.tiffinbox.orderservice.dto.UpdateStatusRequest;
import com.tiffinbox.orderservice.exception.ForbiddenException;
import com.tiffinbox.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Owner order-management endpoints. Requires the gateway-forwarded X-User-Role = OWNER.
 */
@RestController
@RequestMapping("/api/orders/owner")
@RequiredArgsConstructor
public class OwnerOrderController {

    private final OrderService orderService;

    @GetMapping("/today")
    public ResponseEntity<List<OrderResponse>> today(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwner(role);
        return ResponseEntity.ok(orderService.getTodaysOrders().stream()
                .map(OrderResponse::from).toList());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        requireOwner(role);
        return ResponseEntity.ok(OrderResponse.from(orderService.updateStatus(id, request.status())));
    }

    @GetMapping("/summary")
    public ResponseEntity<OrderSummaryResponse> summary(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwner(role);
        return ResponseEntity.ok(orderService.summary());
    }

    private void requireOwner(String role) {
        if (!"OWNER".equals(role)) {
            throw new ForbiddenException("Only the owner can manage orders");
        }
    }
}
