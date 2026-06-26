package com.tiffinbox.orderservice.controller;

import com.tiffinbox.orderservice.dto.OrderResponse;
import com.tiffinbox.orderservice.dto.PlaceOrderRequest;
import com.tiffinbox.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing order endpoints. Identity comes from the gateway-forwarded
 * X-User-Id / X-User-Email headers (the JWT was validated at the edge).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> place(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @Valid @RequestBody PlaceOrderRequest request) {
        var order = orderService.placeOrder(request, userId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> myOrders(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getByUser(userId).stream()
                .map(OrderResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getOwnedById(id, userId)));
    }
}
