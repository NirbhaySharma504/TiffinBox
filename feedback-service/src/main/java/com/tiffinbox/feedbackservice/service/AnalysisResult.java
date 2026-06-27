package com.tiffinbox.feedbackservice.service;

import com.tiffinbox.feedbackservice.entity.Sentiment;

import java.util.List;

/** Internal result of analyzing a comment — from the AI model or the keyword fallback. */
public record AnalysisResult(Sentiment sentiment, List<String> themes, String summary, boolean byAi) {
}
