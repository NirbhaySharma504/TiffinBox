package com.tiffinbox.paymentservice.repository;

import com.tiffinbox.paymentservice.entity.Payment;
import com.tiffinbox.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByUserId(Long userId);

    long countByStatus(PaymentStatus status);

    /** Total amount actually collected (PAID). Returns null if none, handled in the service. */
    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(PaymentStatus status);
}
