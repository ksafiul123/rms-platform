package com.rms.service.notification;

import com.rms.entity.Order;
import com.rms.entity.DeliveryAssignment;
import com.rms.entity.User;
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

    public void sendDeliveryAssignment(User deliveryPartner, Order order) {
        log.info("Sending delivery assignment to {} for order {}", deliveryPartner.getId(), order.getOrderNumber());
    }

    public void sendDeliveryPartnerAssigned(Order order, User deliveryPartner) {
        log.info("Delivery partner {} assigned to order {}", deliveryPartner.getId(), order.getOrderNumber());
    }

    public void sendOrderUpdate(Order order, String message) {
        log.info("Order update for {}: {}", order.getOrderNumber(), message);
    }

    public void sendOrderOutForDelivery(Order order, DeliveryAssignment assignment) {
        log.info("Order {} out for delivery with assignment {}", order.getOrderNumber(), assignment.getId());
    }

    public void sendOrderDelivered(Order order) {
        log.info("Order {} delivered", order.getOrderNumber());
    }
}
