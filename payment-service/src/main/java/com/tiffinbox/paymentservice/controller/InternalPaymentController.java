package com.tiffinbox.paymentservice.controller;

import com.tiffinbox.paymentservice.dto.CreatePaymentRequest;
import com.tiffinbox.paymentservice.dto.PaymentResponse;
import com.tiffinbox.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoint called by order-service via Feign when an order is placed.
 */
@RestController
@RequestMapping("/api/payments/internal")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentResponse.from(paymentService.create(request)));
    }
}
