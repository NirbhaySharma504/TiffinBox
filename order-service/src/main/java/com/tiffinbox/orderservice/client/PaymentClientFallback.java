package com.tiffinbox.orderservice.client;

import com.tiffinbox.orderservice.client.dto.CreatePaymentRequest;
import com.tiffinbox.orderservice.client.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Payment creation is NON-fatal: if payment-service is down we still keep the order
 * (payment stays pending, paymentId null) instead of failing the whole placement.
 * Returning null signals "payment not recorded" to the service layer.
 */
@Component
public class PaymentClientFallback implements PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClientFallback.class);

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.warn("payment-service unavailable — order {} placed without a payment record",
                request.orderId());
        return null;
    }
}
