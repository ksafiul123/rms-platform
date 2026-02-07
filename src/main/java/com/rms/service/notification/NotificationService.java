package com.rms.service.notification;

import com.rms.entity.DeliveryAssignment;
import com.rms.entity.Order;
import com.rms.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendDeliveryAssignment(User deliveryPartner, Order order) {
        log.info("Sending delivery assignment to partner {} for order {}",
                deliveryPartner.getId(), order.getOrderNumber());
    }

    public void sendDeliveryPartnerAssigned(Order order, User deliveryPartner) {
        log.info("Notifying customer about delivery partner {} assigned for order {}",
                deliveryPartner.getId(), order.getOrderNumber());
    }

    public void sendOrderStatusUpdate(Order order, String message) {
        log.info("Sending order status update for order {}: {}", order.getOrderNumber(), message);
    }

    public void sendOrderUpdate(Order order, String message) {
        log.info("Sending order update for order {}: {}", order.getOrderNumber(), message);
    }

    public void sendOrderReadyNotification(Order order) {
        log.info("Sending order ready notification for order {}", order.getOrderNumber());
    }

    public void sendOrderOutForDelivery(Order order, DeliveryAssignment assignment) {
        log.info("Sending out-for-delivery notification for order {} with assignment {}",
                order.getOrderNumber(), assignment.getId());
    }

    public void sendOrderDelivered(Order order) {
        log.info("Sending order delivered notification for order {}", order.getOrderNumber());
    }
}
