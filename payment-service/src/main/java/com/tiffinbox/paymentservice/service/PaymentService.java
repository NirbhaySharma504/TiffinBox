package com.tiffinbox.paymentservice.service;

import com.tiffinbox.paymentservice.dto.CreatePaymentRequest;
import com.tiffinbox.paymentservice.dto.PaymentSummaryResponse;
import com.tiffinbox.paymentservice.entity.Payment;
import com.tiffinbox.paymentservice.entity.PaymentStatus;
import com.tiffinbox.paymentservice.exception.PaymentStateException;
import com.tiffinbox.paymentservice.exception.ResourceNotFoundException;
import com.tiffinbox.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment create(CreatePaymentRequest request) {
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .userId(request.userId())
                .amount(request.amount())
                .method(request.method())
                .status(PaymentStatus.PENDING)
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    @Transactional(readOnly = true)
    public Payment getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Payment> getByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    /** Mock "pay now": flips PENDING -> PAID, stamps a transaction ref and time. */
    @Transactional
    public Payment markPaid(Long id) {
        Payment payment = getById(id);
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new PaymentStateException("Payment " + id + " is already paid");
        }
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        payment.setPaidAt(Instant.now());
        return payment;
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse summary() {
        return new PaymentSummaryResponse(
                paymentRepository.sumAmountByStatus(PaymentStatus.PAID),
                paymentRepository.countByStatus(PaymentStatus.PAID),
                paymentRepository.countByStatus(PaymentStatus.PENDING),
                paymentRepository.countByStatus(PaymentStatus.FAILED)
        );
    }
}
