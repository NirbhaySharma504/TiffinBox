package com.tiffinbox.feedbackservice.service;

import com.tiffinbox.feedbackservice.dto.FeedbackSummaryResponse;
import com.tiffinbox.feedbackservice.dto.SubmitFeedbackRequest;
import com.tiffinbox.feedbackservice.entity.Feedback;
import com.tiffinbox.feedbackservice.entity.Sentiment;
import com.tiffinbox.feedbackservice.exception.DuplicateFeedbackException;
import com.tiffinbox.feedbackservice.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SentimentAnalyzer sentimentAnalyzer;

    /**
     * Not @Transactional on purpose: the AI/network call must not run inside a DB
     * transaction. We analyze first, then persist (repository.save is its own short tx);
     * the (order_id, user_id) unique constraint guards against duplicate submissions.
     */
    public Feedback submit(SubmitFeedbackRequest request, Long userId) {
        if (feedbackRepository.existsByOrderIdAndUserId(request.orderId(), userId)) {
            throw new DuplicateFeedbackException("Feedback already submitted for order " + request.orderId());
        }

        AnalysisResult analysis = sentimentAnalyzer.analyze(request.comment(), request.rating());

        Feedback feedback = Feedback.builder()
                .orderId(request.orderId())
                .userId(userId)
                .rating(request.rating())
                .comment(request.comment())
                .sentiment(analysis.sentiment())
                .themes(analysis.themes().isEmpty() ? null : String.join(",", analysis.themes()))
                .aiSummary(analysis.summary())
                .analyzedByAi(analysis.byAi())
                .build();

        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getByUser(Long userId) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Feedback> getAll() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    public FeedbackSummaryResponse summary() {
        List<Feedback> all = feedbackRepository.findAll();

        // Tally theme frequency across all feedback, keep the most common.
        Map<String, Long> themeCounts = all.stream()
                .filter(f -> f.getThemes() != null && !f.getThemes().isBlank())
                .flatMap(f -> List.of(f.getThemes().split("\\s*,\\s*")).stream())
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        List<String> topThemes = themeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(java.util.ArrayList::new));

        return new FeedbackSummaryResponse(
                all.size(),
                feedbackRepository.countBySentiment(Sentiment.POSITIVE),
                feedbackRepository.countBySentiment(Sentiment.NEUTRAL),
                feedbackRepository.countBySentiment(Sentiment.NEGATIVE),
                feedbackRepository.countBySentiment(Sentiment.UNKNOWN),
                topThemes);
    }
}
