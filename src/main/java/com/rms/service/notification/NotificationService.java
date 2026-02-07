package com.rms.service.notification;

import com.rms.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendOrderStatusUpdate(Order order, String message) {
        log.info("Sending order status update for order {}: {}", order.getOrderNumber(), message);
    }

    public void sendOrderReadyNotification(Order order) {
        log.info("Sending order ready notification for order {}", order.getOrderNumber());
    }
}
