package com.tiffinbox.feedbackservice.controller;

import com.tiffinbox.feedbackservice.dto.FeedbackResponse;
import com.tiffinbox.feedbackservice.dto.SubmitFeedbackRequest;
import com.tiffinbox.feedbackservice.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer feedback endpoints. Identity comes from the gateway-forwarded X-User-Id header.
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> submit(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        var feedback = feedbackService.submit(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(FeedbackResponse.from(feedback));
    }

    @GetMapping("/me")
    public ResponseEntity<List<FeedbackResponse>> mine(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(feedbackService.getByUser(userId).stream()
                .map(FeedbackResponse::from).toList());
    }
}
