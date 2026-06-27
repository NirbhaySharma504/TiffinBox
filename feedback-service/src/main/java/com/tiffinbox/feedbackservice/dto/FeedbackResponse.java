package com.tiffinbox.feedbackservice.dto;

import com.tiffinbox.feedbackservice.entity.Feedback;
import com.tiffinbox.feedbackservice.entity.Sentiment;

import java.time.Instant;
import java.util.List;

public record FeedbackResponse(
        Long id,
        Long orderId,
        Long userId,
        Integer rating,
        String comment,
        Sentiment sentiment,
        List<String> themes,
        String aiSummary,
        boolean analyzedByAi,
        Instant createdAt
) {
    public static FeedbackResponse from(Feedback f) {
        List<String> themes = (f.getThemes() == null || f.getThemes().isBlank())
                ? List.of()
                : List.of(f.getThemes().split("\\s*,\\s*"));
        return new FeedbackResponse(
                f.getId(), f.getOrderId(), f.getUserId(), f.getRating(), f.getComment(),
                f.getSentiment(), themes, f.getAiSummary(), f.isAnalyzedByAi(), f.getCreatedAt());
    }
}
