package com.rms.service;

//package com.rms.service;

import com.rms.dto.order.OrderDTO.*;
import com.rms.entity.*;
import com.rms.exception.BadRequestException;
import com.rms.exception.ForbiddenException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.TableSessionRepository;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Extension to OrderService for table session integration
 * Add these methods to the existing OrderService class
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTableIntegration {

    private final TableSessionRepository sessionRepository;

    /**
     * Create order within a table session (QR-based ordering)
     * <p>
     * Usage: Add this method to OrderService
     */
    public OrderResponse createSessionOrder(Long sessionId, CreateOrderRequest request,
                                            UserPrincipal currentUser) {
        log.info("Creating order for session {} by user {}", sessionId, currentUser.getId());

        // Verify session exists and is active
        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.getStatus() != TableSession.SessionStatus.ACTIVE) {
            throw new BadRequestException("Session is not active");
        }

        // Verify user is part of the session
        boolean isGuest = session.getGuests().stream()
                .anyMatch(g -> g.getUserId() != null &&
                        g.getUserId().equals(currentUser.getId()) &&
                        g.getStatus() == TableSessionGuest.GuestStatus.ACTIVE);

        if (!isGuest && !currentUser.hasAnyRole("RESTAURANT_ADMIN", "ADMIN")) {
            throw new ForbiddenException("You are not part of this session");
        }

        // Override order type to DINE_IN for session orders
        request.setOrderType(Order.OrderType.DINE_IN);
        request.setTableNumber(session.getTable().getTableNumber());

        // Create order using existing logic
        Order order = new Order();
        order.setRestaurantId(currentUser.getRestaurantId());
        order.setCustomerId(currentUser.getId());
        order.setOrderNumber(generateOrderNumber());
        order.setOrderType(Order.OrderType.DINE_IN);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTableNumber(session.getTable().getTableNumber());
        order.setTableSession(session); // Link to session

        // ... continue with existing order creation logic ...

        // Add order to session
        session.addOrder(order);
        sessionRepository.save(session);

        return mapToOrderResponse(order);
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(new java.util.Random().nextInt(1000));
        return "ORD" + timestamp.substring(timestamp.length() - 8) + random;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setRestaurantId(order.getRestaurantId());
        response.setCustomerId(order.getCustomerId());
        response.setOrderNumber(order.getOrderNumber());
        response.setOrderType(order.getOrderType());
        response.setStatus(order.getStatus());
        response.setTableNumber(order.getTableNumber());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliveryManId(order.getDeliveryManId());
        response.setSubtotal(order.getSubtotal());
        response.setTaxAmount(order.getTaxAmount());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setTotalAmount(order.getTotalAmount());
        response.setSpecialInstructions(order.getSpecialInstructions());
        response.setEstimatedReadyTime(order.getEstimatedReadyTime());
        response.setActualReadyTime(order.getActualReadyTime());
        response.setDeliveryTime(order.getDeliveryTime());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }
}




