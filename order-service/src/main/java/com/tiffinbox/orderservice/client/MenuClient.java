package com.tiffinbox.orderservice.client;

import com.tiffinbox.orderservice.client.dto.ValidateItemsRequest;
import com.tiffinbox.orderservice.client.dto.ValidatedItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Declarative HTTP client to menu-service, resolved via Eureka (no hardcoded URL).
 * Backed by a circuit breaker (feign.circuitbreaker.enabled); on failure the call
 * routes to {@link MenuClientFallback}.
 */
@FeignClient(name = "menu-service", fallback = MenuClientFallback.class)
public interface MenuClient {

    @PostMapping("/api/menu/internal/validate-items")
    List<ValidatedItemResponse> validateItems(@RequestBody ValidateItemsRequest request);
}
