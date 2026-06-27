package com.tiffinbox.feedbackservice.service;

import com.tiffinbox.feedbackservice.entity.Sentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Classifies feedback sentiment + themes. Uses the configured LLM (Groq via Spring AI)
 * when enabled; on any failure — or when AI is disabled / no key — it degrades to a
 * simple keyword + rating heuristic so the service never hard-fails on the AI path.
 */
@Service
public class SentimentAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(SentimentAnalyzer.class);

    private static final Set<String> POSITIVE_WORDS = Set.of(
            "good", "great", "love", "loved", "excellent", "tasty", "delicious",
            "amazing", "fresh", "happy", "best", "perfect", "yummy", "nice");
    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "bad", "terrible", "worst", "late", "cold", "stale", "awful",
            "disappointed", "rude", "missing", "wrong", "soggy", "bland", "horrible");

    private final ChatClient chatClient; // null if no ChatModel bean
    private final boolean aiEnabled;

    public SentimentAnalyzer(ObjectProvider<ChatModel> chatModelProvider,
                             @Value("${feedback.ai.enabled}") boolean aiEnabled) {
        ChatModel model = chatModelProvider.getIfAvailable();
        this.chatClient = model != null ? ChatClient.create(model) : null;
        this.aiEnabled = aiEnabled;
    }

    public AnalysisResult analyze(String comment, Integer rating) {
        if (aiEnabled && chatClient != null) {
            try {
                return analyzeWithAi(comment, rating);
            } catch (Exception ex) {
                log.warn("AI sentiment analysis failed, falling back to keywords: {}", ex.getMessage());
            }
        }
        return keywordFallback(comment, rating);
    }

    private AnalysisResult analyzeWithAi(String comment, Integer rating) {
        AiAnalysis a = chatClient.prompt()
                .system("""
                        You analyze customer feedback for a home-style tiffin (meal) delivery service.
                        Classify the overall sentiment as exactly one of: POSITIVE, NEUTRAL, NEGATIVE.
                        Extract up to 3 short lowercase themes (e.g. "delivery time", "portion size", "taste").
                        Write a concise one-sentence summary. Base it only on the feedback provided.
                        """)
                .user(u -> u.text("Rating (1-5, optional): {rating}\nFeedback: {comment}")
                        .param("rating", rating == null ? "n/a" : rating.toString())
                        .param("comment", comment))
                .call()
                .entity(AiAnalysis.class);

        List<String> themes = a.themes() == null ? List.of() : a.themes();
        return new AnalysisResult(parseSentiment(a.sentiment()), themes, a.summary(), true);
    }

    private Sentiment parseSentiment(String raw) {
        if (raw == null) return Sentiment.NEUTRAL;
        return switch (raw.trim().toUpperCase()) {
            case "POSITIVE" -> Sentiment.POSITIVE;
            case "NEGATIVE" -> Sentiment.NEGATIVE;
            default -> Sentiment.NEUTRAL;
        };
    }

    private AnalysisResult keywordFallback(String comment, Integer rating) {
        String text = comment == null ? "" : comment.toLowerCase();
        long pos = POSITIVE_WORDS.stream().filter(text::contains).count();
        long neg = NEGATIVE_WORDS.stream().filter(text::contains).count();

        // Rating is a strong signal when present.
        if (rating != null) {
            if (rating >= 4) pos += 2;
            if (rating <= 2) neg += 2;
        }

        Sentiment sentiment;
        if (pos > neg) sentiment = Sentiment.POSITIVE;
        else if (neg > pos) sentiment = Sentiment.NEGATIVE;
        else sentiment = Sentiment.NEUTRAL;

        List<String> themes = new ArrayList<>();
        POSITIVE_WORDS.stream().filter(text::contains).limit(2).forEach(themes::add);
        NEGATIVE_WORDS.stream().filter(text::contains).limit(2).forEach(themes::add);

        return new AnalysisResult(sentiment, themes, null, false);
    }
}
