package com.tiffinbox.orderservice.client;

import com.tiffinbox.orderservice.client.dto.ValidateItemsRequest;
import com.tiffinbox.orderservice.client.dto.ValidatedItemResponse;
import com.tiffinbox.orderservice.exception.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Menu validation is REQUIRED to place an order, so the fallback fails fast rather than
 * letting an unvalidated order through. The circuit breaker stops us hammering a dead
 * menu-service and surfaces a clean 503 to the caller.
 */
@Component
public class MenuClientFallback implements MenuClient {

    @Override
    public List<ValidatedItemResponse> validateItems(ValidateItemsRequest request) {
        throw new DependencyUnavailableException(
                "menu-service is unavailable — cannot validate items, order not placed");
    }
}
