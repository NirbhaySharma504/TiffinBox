package com.tiffinbox.feedbackservice.dto;

import java.util.List;

/** Owner dashboard rollup: counts per sentiment + the most common themes. */
public record FeedbackSummaryResponse(
        long total,
        long positive,
        long neutral,
        long negative,
        long unknown,
        List<String> topThemes
) {
}
