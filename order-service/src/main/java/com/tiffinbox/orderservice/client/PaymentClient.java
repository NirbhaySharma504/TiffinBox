package com.tiffinbox.orderservice.client;

import com.tiffinbox.orderservice.client.dto.CreatePaymentRequest;
import com.tiffinbox.orderservice.client.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping("/api/payments/internal")
    PaymentResponse createPayment(@RequestBody CreatePaymentRequest request);
}
