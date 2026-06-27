package com.tiffinbox.feedbackservice.controller;

import com.tiffinbox.feedbackservice.dto.FeedbackResponse;
import com.tiffinbox.feedbackservice.dto.FeedbackSummaryResponse;
import com.tiffinbox.feedbackservice.exception.ForbiddenException;
import com.tiffinbox.feedbackservice.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Owner dashboard endpoints. Requires the gateway-forwarded X-User-Role = OWNER.
 */
@RestController
@RequestMapping("/api/feedback/owner")
@RequiredArgsConstructor
public class OwnerFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> all(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwner(role);
        return ResponseEntity.ok(feedbackService.getAll().stream()
                .map(FeedbackResponse::from).toList());
    }

    @GetMapping("/summary")
    public ResponseEntity<FeedbackSummaryResponse> summary(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwner(role);
        return ResponseEntity.ok(feedbackService.summary());
    }

    private void requireOwner(String role) {
        if (!"OWNER".equals(role)) {
            throw new ForbiddenException("Only the owner can view all feedback");
        }
    }
}
