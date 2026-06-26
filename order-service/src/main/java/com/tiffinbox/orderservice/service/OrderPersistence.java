package com.tiffinbox.orderservice.service;

import com.tiffinbox.orderservice.client.dto.ValidatedItemResponse;
import com.tiffinbox.orderservice.dto.OrderItemRequest;
import com.tiffinbox.orderservice.dto.PlaceOrderRequest;
import com.tiffinbox.orderservice.entity.Order;
import com.tiffinbox.orderservice.entity.OrderItem;
import com.tiffinbox.orderservice.entity.OrderStatus;
import com.tiffinbox.orderservice.exception.BadRequestException;
import com.tiffinbox.orderservice.exception.ResourceNotFoundException;
import com.tiffinbox.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Owns the LOCAL database transactions for orders. Kept as a separate bean (not private
 * methods on OrderService) on purpose: Spring's @Transactional only applies through the
 * proxy, so a self-invoked method would silently run without a transaction. Crucially,
 * NO Feign/remote calls happen inside these methods — remote calls stay in the
 * orchestrator (OrderService) so we never hold a DB transaction open across the network.
 */
@Service
@RequiredArgsConstructor
public class OrderPersistence {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(PlaceOrderRequest req, Long userId, String email,
                             Map<Long, ValidatedItemResponse> validatedById) {
        Order order = Order.builder()
                .userId(userId)
                .customerEmail(email)
                .menuId(req.menuId())
                .status(OrderStatus.PLACED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest ir : req.items()) {
            ValidatedItemResponse v = validatedById.get(ir.menuItemId());
            if (v == null) {
                throw new BadRequestException("Item " + ir.menuItemId() + " is not valid for this menu");
            }
            BigDecimal subtotal = v.price().multiply(BigDecimal.valueOf(ir.quantity()));
            order.addItem(OrderItem.builder()
                    .menuItemId(v.itemId())
                    .itemName(v.name())   // denormalized name
                    .price(v.price())     // denormalized price at order time
                    .quantity(ir.quantity())
                    .subtotal(subtotal)
                    .build());
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    @Transactional
    public void updatePaymentId(Long orderId, Long paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setPaymentId(paymentId);
        orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
