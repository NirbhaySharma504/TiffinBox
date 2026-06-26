package com.tiffinbox.subscriptionservice.client;

import com.tiffinbox.subscriptionservice.client.dto.OrderLite;
import com.tiffinbox.subscriptionservice.client.dto.PlaceOrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Places orders on behalf of subscribers by calling order-service's existing customer
 * endpoint directly (Feign bypasses the gateway), supplying the trusted X-User-* headers
 * that the gateway would normally inject. No order-service change needed.
 */
@FeignClient(name = "order-service", fallback = OrderClientFallback.class)
public interface OrderClient {

    @PostMapping("/api/orders")
    OrderLite placeOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Email") String email,
            @RequestBody PlaceOrderRequest request);
}
