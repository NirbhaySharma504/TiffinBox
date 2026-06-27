package com.tiffinbox.feedbackservice.repository;

import com.tiffinbox.feedbackservice.entity.Feedback;
import com.tiffinbox.feedbackservice.entity.Sentiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Feedback> findAllByOrderByCreatedAtDesc();

    boolean existsByOrderIdAndUserId(Long orderId, Long userId);

    long countBySentiment(Sentiment sentiment);
}
