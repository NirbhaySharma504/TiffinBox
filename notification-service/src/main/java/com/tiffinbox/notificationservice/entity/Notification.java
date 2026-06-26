package com.tiffinbox.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        // Idempotency backstop: at most one notification per (order, type).
        uniqueConstraints = @UniqueConstraint(columnNames = {"orderId", "type"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The order this notification is about (from the order-placed event).
     * Nullable so ad-hoc/manual notifications don't need an order; Postgres treats
     * NULLs as distinct, so the (orderId, type) unique constraint still dedupes events.
     */
    private Long orderId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    /** Populated when sending fails, for debugging. */
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
