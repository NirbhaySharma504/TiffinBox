package com.tiffinbox.feedbackservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "feedback",
        uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Optional 1–5 star rating. */
    private Integer rating;

    @Column(nullable = false, columnDefinition = "text")
    private String comment;

    // ---- AI-derived fields ----

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sentiment sentiment;

    /** Comma-separated themes extracted from the comment (e.g. "delivery,portion size"). */
    @Column(columnDefinition = "text")
    private String themes;

    /** One-line AI summary of the feedback. */
    @Column(columnDefinition = "text")
    private String aiSummary;

    /** True if a real AI model produced the analysis; false if the keyword fallback did. */
    @Column(nullable = false)
    private boolean analyzedByAi;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.sentiment == null) {
            this.sentiment = Sentiment.UNKNOWN;
        }
    }
}
