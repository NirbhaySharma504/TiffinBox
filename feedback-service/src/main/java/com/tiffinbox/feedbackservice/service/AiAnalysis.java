package com.tiffinbox.feedbackservice.service;

import java.util.List;

/**
 * Shape the LLM is asked to return (Spring AI maps the JSON response onto this record).
 * {@code sentiment} is one of POSITIVE / NEUTRAL / NEGATIVE.
 */
public record AiAnalysis(String sentiment, List<String> themes, String summary) {
}
