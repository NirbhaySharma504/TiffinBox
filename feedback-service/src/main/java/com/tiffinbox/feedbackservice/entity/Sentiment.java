package com.tiffinbox.feedbackservice.entity;

/** Sentiment classification of a piece of feedback. UNKNOWN if analysis couldn't run. */
public enum Sentiment {
    POSITIVE,
    NEUTRAL,
    NEGATIVE,
    UNKNOWN
}
