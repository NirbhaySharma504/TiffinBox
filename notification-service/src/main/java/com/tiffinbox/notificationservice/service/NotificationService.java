package com.tiffinbox.notificationservice.service;

import com.tiffinbox.notificationservice.dto.OrderPlacedEvent;
import com.tiffinbox.notificationservice.dto.SendNotificationRequest;
import com.tiffinbox.notificationservice.entity.Notification;
import com.tiffinbox.notificationservice.entity.NotificationStatus;
import com.tiffinbox.notificationservice.entity.NotificationType;
import com.tiffinbox.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    /**
     * Handles an order-placed event idempotently: if a notification for this order
     * already exists, it is skipped (Kafka is at-least-once, so duplicates happen).
     */
    @Transactional
    public void processOrderPlaced(OrderPlacedEvent event) {
        if (event.orderId() == null) {
            log.warn("Ignoring order-placed event with no orderId: {}", event);
            return;
        }
        if (notificationRepository.existsByOrderIdAndType(event.orderId(), NotificationType.ORDER_PLACED)) {
            log.info("Duplicate order-placed event for order {} — already notified, skipping", event.orderId());
            return;
        }

        String name = event.customerName() != null ? event.customerName() : "there";
        String subject = "Your TiffinBox order #" + event.orderId() + " is confirmed";
        String body = "Hi " + name + ",\n\nYour order #" + event.orderId()
                + " has been placed successfully"
                + (event.totalAmount() != null ? " for a total of ₹" + event.totalAmount() : "")
                + ".\n\nThank you for ordering with TiffinBox!";

        Notification notification = Notification.builder()
                .orderId(event.orderId())
                .userId(event.userId())
                .type(NotificationType.ORDER_PLACED)
                .recipientEmail(event.customerEmail())
                .subject(subject)
                .body(body)
                .build();

        deliver(notification);

        try {
            notificationRepository.save(notification);
            log.info("Recorded ORDER_PLACED notification for order {} (status={})",
                    event.orderId(), notification.getStatus());
        } catch (DataIntegrityViolationException dup) {
            // Concurrent duplicate slipped past the existence check — unique constraint caught it.
            log.info("Concurrent duplicate for order {} — unique constraint enforced idempotency",
                    event.orderId());
        }
    }

    @Transactional
    public Notification sendManual(SendNotificationRequest request) {
        Notification notification = Notification.builder()
                .orderId(request.orderId())
                .userId(request.userId())
                .type(NotificationType.ORDER_PLACED)
                .recipientEmail(request.recipientEmail())
                .subject(request.subject())
                .body(request.body())
                .build();
        deliver(notification);
        return notificationRepository.save(notification);
    }

    /** Attempts delivery and stamps the notification's status. Never throws. */
    private void deliver(Notification notification) {
        try {
            emailService.send(notification.getRecipientEmail(),
                    notification.getSubject(), notification.getBody());
            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(ex.getMessage());
            log.error("Failed to send notification to {}: {}",
                    notification.getRecipientEmail(), ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getByUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }
}
