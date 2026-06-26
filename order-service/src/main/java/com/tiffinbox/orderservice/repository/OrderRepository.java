package com.tiffinbox.orderservice.repository;

import com.tiffinbox.orderservice.entity.Order;
import com.tiffinbox.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // EntityGraph eager-loads items so entities can be mapped to DTOs after the
    // transaction closes (open-in-view is disabled).

    @EntityGraph(attributePaths = "items")
    @Override
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = "items")
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "items")
    List<Order> findByCreatedAtAfterOrderByCreatedAtDesc(Instant after);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status <> 'CANCELLED'")
    java.math.BigDecimal totalRevenue();
}
