package com.tiffinbox.paymentservice.controller;

import com.tiffinbox.paymentservice.dto.PaymentSummaryResponse;
import com.tiffinbox.paymentservice.exception.ForbiddenException;
import com.tiffinbox.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Owner dashboard endpoints. Requires the gateway-forwarded X-User-Role to be OWNER.
 */
@RestController
@RequestMapping("/api/payments/owner")
@RequiredArgsConstructor
public class OwnerPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/summary")
    public ResponseEntity<PaymentSummaryResponse> summary(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"OWNER".equals(role)) {
            throw new ForbiddenException("Only the owner can view payment summaries");
        }
        return ResponseEntity.ok(paymentService.summary());
    }
}
