package com.tiffinbox.subscriptionservice.controller;

import com.tiffinbox.subscriptionservice.dto.CreateSubscriptionRequest;
import com.tiffinbox.subscriptionservice.dto.SubscriptionResponse;
import com.tiffinbox.subscriptionservice.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer subscription management. Identity from the gateway-forwarded X-User-* headers.
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        var sub = subscriptionService.create(request, userId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionResponse.from(sub));
    }

    @GetMapping("/me")
    public ResponseEntity<List<SubscriptionResponse>> mine(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(subscriptionService.getByUser(userId).stream()
                .map(SubscriptionResponse::from).toList());
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<SubscriptionResponse> pause(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(SubscriptionResponse.from(subscriptionService.pause(id, userId)));
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<SubscriptionResponse> resume(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(SubscriptionResponse.from(subscriptionService.resume(id, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> cancel(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(SubscriptionResponse.from(subscriptionService.cancel(id, userId)));
    }
}
