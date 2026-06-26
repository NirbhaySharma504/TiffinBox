package com.tiffinbox.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiffinbox.notificationservice.dto.OrderPlacedEvent;
import com.tiffinbox.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code order-placed} events from the {@code order-events} topic and turns
 * them into notifications. Messages are JSON strings (decoupled from the producer's
 * class), parsed here with the Spring-managed ObjectMapper.
 */
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${notification.kafka.order-events-topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderPlaced(String message) {
        log.info("Received order-events message: {}", message);
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            notificationService.processOrderPlaced(event);
        } catch (Exception ex) {
            // Bad payloads are logged and dropped rather than poison-pilling the consumer.
            log.error("Failed to process order-events message '{}': {}", message, ex.getMessage());
        }
    }
}
