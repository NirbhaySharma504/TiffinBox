package com.tiffinbox.subscriptionservice.controller;

import com.tiffinbox.subscriptionservice.dto.SubscriptionResponse;
import com.tiffinbox.subscriptionservice.dto.SubscriptionSummaryResponse;
import com.tiffinbox.subscriptionservice.exception.ForbiddenException;
import com.tiffinbox.subscriptionservice.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Owner endpoints. Requires gateway-forwarded X-User-Role = OWNER. Includes an on-demand
 * trigger for the auto-order run (the daily scheduler otherwise drives it).
 */
@RestController
@RequestMapping("/api/subscriptions/owner")
@RequiredArgsConstructor
public class OwnerSubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> all(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwner(role);
        return ResponseEntity.ok(subscriptionService.getAll().stream()
                .map(SubscriptionResponse::from).toList());
    }

    @GetMapping("/summary")
    public ResponseEntity<SubscriptionSummaryResponse> summary(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwner(role);
        return ResponseEntity.ok(subscriptionService.summary());
    }

    @PostMapping("/run-due")
    public ResponseEntity<Map<String, Integer>> runDue(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwner(role);
        int placed = subscriptionService.processDueSubscriptions();
        return ResponseEntity.ok(Map.of("ordersPlaced", placed));
    }

    private void requireOwner(String role) {
        if (!"OWNER".equals(role)) {
            throw new ForbiddenException("Only the owner can access this endpoint");
        }
    }
}
