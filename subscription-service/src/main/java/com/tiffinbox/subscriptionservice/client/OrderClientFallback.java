package com.tiffinbox.subscriptionservice.client;

import com.tiffinbox.subscriptionservice.client.dto.OrderLite;
import com.tiffinbox.subscriptionservice.client.dto.PlaceOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * If order-service is unavailable, return null so the scheduler logs the failure and
 * leaves the subscription un-stamped — it will be retried on the next run.
 */
@Component
public class OrderClientFallback implements OrderClient {

    private static final Logger log = LoggerFactory.getLogger(OrderClientFallback.class);

    @Override
    public OrderLite placeOrder(Long userId, String email, PlaceOrderRequest request) {
        log.warn("order-service unavailable — auto-order for user {} not placed", userId);
        return null;
    }
}
