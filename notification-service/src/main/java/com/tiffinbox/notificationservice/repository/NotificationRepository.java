package com.tiffinbox.notificationservice.repository;

import com.tiffinbox.notificationservice.entity.Notification;
import com.tiffinbox.notificationservice.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Idempotency check: have we already handled this order's notification of this type? */
    boolean existsByOrderIdAndType(Long orderId, NotificationType type);

    List<Notification> findByUserId(Long userId);
}
