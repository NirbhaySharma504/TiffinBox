package com.tiffinbox.orderservice.service;

import com.tiffinbox.orderservice.client.MenuClient;
import com.tiffinbox.orderservice.client.PaymentClient;
import com.tiffinbox.orderservice.client.dto.CreatePaymentRequest;
import com.tiffinbox.orderservice.client.dto.ValidateItemsRequest;
import com.tiffinbox.orderservice.client.dto.ValidatedItemResponse;
import com.tiffinbox.orderservice.dto.OrderItemRequest;
import com.tiffinbox.orderservice.dto.OrderSummaryResponse;
import com.tiffinbox.orderservice.dto.PlaceOrderRequest;
import com.tiffinbox.orderservice.entity.Order;
import com.tiffinbox.orderservice.entity.OrderStatus;
import com.tiffinbox.orderservice.exception.ForbiddenException;
import com.tiffinbox.orderservice.exception.ResourceNotFoundException;
import com.tiffinbox.orderservice.kafka.OrderEventProducer;
import com.tiffinbox.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates order placement. This class is intentionally NOT @Transactional: it
 * sequences remote (Feign) calls and local DB writes so that no remote call ever runs
 * inside a database transaction. Designed for partial failure — see placeOrder.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderPersistence orderPersistence;
    private final MenuClient menuClient;
    private final PaymentClient paymentClient;
    private final OrderEventProducer eventProducer;

    public Order placeOrder(PlaceOrderRequest req, Long userId, String email) {
        // 1. BLOCKING + REQUIRED: validate items with menu-service (Feign, no DB tx).
        //    If menu-service is down, the circuit breaker fallback throws -> 503, no order saved.
        List<Long> itemIds = req.items().stream()
                .map(OrderItemRequest::menuItemId).distinct().toList();
        List<ValidatedItemResponse> validated =
                menuClient.validateItems(new ValidateItemsRequest(req.menuId(), itemIds));
        Map<Long, ValidatedItemResponse> byId = validated.stream()
                .collect(Collectors.toMap(ValidatedItemResponse::itemId, Function.identity()));

        // 2. Persist the order in a short LOCAL transaction (no remote calls inside).
        Order order = orderPersistence.createOrder(req, userId, email, byId);

        // 3. NON-FATAL: create a payment record (Feign, no DB tx). If payment-service is
        //    down the fallback returns null and the order remains payment-pending.
        Long paymentId = createPaymentSafely(order, userId);
        if (paymentId != null) {
            orderPersistence.updatePaymentId(order.getId(), paymentId);
            order.setPaymentId(paymentId);
        }

        // 4. Publish the order-placed event (async) AFTER the order is committed.
        eventProducer.publishOrderPlaced(order);

        return order;
    }

    private Long createPaymentSafely(Order order, Long userId) {
        var resp = paymentClient.createPayment(
                new CreatePaymentRequest(order.getId(), userId, order.getTotalAmount(), "UPI"));
        if (resp == null) {
            log.warn("No payment recorded for order {} (payment-service unavailable)", order.getId());
            return null;
        }
        return resp.id();
    }

    public Order getOwnedById(Long id, Long userId) {
        Order order = getById(id);
        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only view your own orders");
        }
        return order;
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<Order> getByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getTodaysOrders() {
        var startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        return orderRepository.findByCreatedAtAfterOrderByCreatedAtDesc(startOfToday);
    }

    public Order updateStatus(Long orderId, OrderStatus status) {
        return orderPersistence.updateStatus(orderId, status);
    }

    public OrderSummaryResponse summary() {
        return new OrderSummaryResponse(
                orderRepository.countByStatus(OrderStatus.PLACED),
                orderRepository.countByStatus(OrderStatus.PREPARING),
                orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY),
                orderRepository.countByStatus(OrderStatus.DELIVERED),
                orderRepository.countByStatus(OrderStatus.CANCELLED),
                orderRepository.totalRevenue() == null ? BigDecimal.ZERO : orderRepository.totalRevenue()
        );
    }
}
