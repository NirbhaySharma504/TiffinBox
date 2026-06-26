package com.tiffinbox.subscriptionservice.service;

import com.tiffinbox.subscriptionservice.entity.Subscription;
import com.tiffinbox.subscriptionservice.exception.ResourceNotFoundException;
import com.tiffinbox.subscriptionservice.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Small transactional writer kept separate from the scheduler orchestration so the
 * Feign calls (menu/order) never run inside a DB transaction, and so the stamp update
 * goes through the Spring proxy (not a self-invocation).
 */
@Service
@RequiredArgsConstructor
public class SubscriptionWriter {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void markOrdered(Long subscriptionId, LocalDate date) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
        sub.setLastOrderedOn(date);
        subscriptionRepository.save(sub);
    }
}
