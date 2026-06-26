package com.tiffinbox.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiffinbox.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes the order-placed event as a JSON string keyed by order id. We use Kafka
 * (async) rather than a Feign call to notification-service so order placement does not
 * block on, or fail because of, email delivery.
 */
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${order.kafka.order-events-topic}")
    private String topic;

    public void publishOrderPlaced(Order order) {
        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getUserId(),
                order.getCustomerEmail(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(order.getId()), json);
            log.info("Published order-placed event for order {}", order.getId());
        } catch (Exception ex) {
            // Publishing failure must not roll back an already-committed order.
            log.error("Failed to publish order-placed event for order {}: {}",
                    order.getId(), ex.getMessage());
        }
    }
}
