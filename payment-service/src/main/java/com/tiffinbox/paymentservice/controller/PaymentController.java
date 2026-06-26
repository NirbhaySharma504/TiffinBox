package com.tiffinbox.paymentservice.controller;

import com.tiffinbox.paymentservice.dto.PaymentResponse;
import com.tiffinbox.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing payment endpoints (reached through the gateway; identity comes from
 * the gateway-forwarded X-User-Id header).
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.getById(id)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.getByOrderId(orderId)));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> myPayments(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(paymentService.getByUser(userId).stream()
                .map(PaymentResponse::from).toList());
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentResponse> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.markPaid(id)));
    }
}
