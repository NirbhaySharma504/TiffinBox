package com.tiffinbox.subscriptionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    /** Denormalized so auto-placed orders carry the customer's email without a user-service call. */
    @Column(nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    /** Optional end date; null = open-ended. */
    private LocalDate endDate;

    /** Last date an order was auto-placed — prevents double-ordering on the same day. */
    private LocalDate lastOrderedOn;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = SubscriptionStatus.ACTIVE;
        }
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }

    /** Whether this subscription should auto-order on the given date. */
    public boolean isDueOn(LocalDate date) {
        if (status != SubscriptionStatus.ACTIVE) {
            return false;
        }
        if (date.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        if (date.equals(lastOrderedOn)) {
            return false; // already ordered today
        }
        return frequency.appliesOn(date);
    }
}
