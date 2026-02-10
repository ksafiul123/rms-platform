package com.rms.service.delivery;

// DeliveryService.java
//package com.rms.service.delivery;

import com.rms.dto.delivery.*;
import com.rms.entity.*;
import com.rms.enums.RoleName;
import com.rms.exception.*;
import com.rms.repository.*;
import com.rms.security.SecurityUtil;
import com.rms.service.notification.NotificationService;
import com.rms.service.tracking.OrderTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryService {

    private final OrderRepository orderRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final UserRepository userRepository;
    private final OrderTimelineService timelineService;
    private final NotificationService notificationService;

    public List<DeliveryOrderResponse> getAvailableDeliveryOrders(Long restaurantId) {

        log.info("Fetching available delivery orders for restaurant: {}", restaurantId);

        List<Order> readyOrders = orderRepository.findByRestaurantIdAndStatusAndOrderType(
                restaurantId,
                Order.OrderStatus.READY,
                Order.OrderType.DELIVERY
        );

        return readyOrders.stream()
                .filter(order -> deliveryAssignmentRepository.findByOrderId(order.getId()).isEmpty())
                .map(this::mapToDeliveryOrderResponse)
                .collect(Collectors.toList());
    }

    public DeliveryAssignmentResponse assignDeliveryPartner(
            Long orderId, Long deliveryPartnerId, Long assignedBy) {

        log.info("Assigning delivery partner: orderId={}, partnerId={}", orderId, deliveryPartnerId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getOrderType() != Order.OrderType.DELIVERY) {
            throw new BadRequestException("Order is not a delivery order");
        }

        if (deliveryAssignmentRepository.findByOrderId(orderId).isPresent()) {
            throw new BadRequestException("Delivery partner already assigned to this order");
        }

        User deliveryPartner = userRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found: " + deliveryPartnerId));

        boolean isDeliveryPartner = deliveryPartner.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ROLE_DELIVERY_MAN);

        if (!isDeliveryPartner) {
            throw new BadRequestException("User is not a delivery partner");
        }

        long activeDeliveries = deliveryAssignmentRepository
                .countActiveDeliveriesForPartner(deliveryPartnerId);

        if (activeDeliveries >= 3) {
            throw new BadRequestException("Delivery partner has too many active deliveries");
        }

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .order(order)
                .deliveryPartner(deliveryPartner)
                .status(DeliveryAssignment.DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .estimatedPickupTime(LocalDateTime.now().plusMinutes(10))
                .estimatedDeliveryTime(calculateEstimatedDeliveryTime(order))
                .build();

        deliveryAssignmentRepository.save(assignment);

        timelineService.addEvent(order, OrderTimeline.EventType.DELIVERY_ASSIGNED,
                "Delivery partner assigned",
                "Your delivery partner will pick up the order shortly");

        notificationService.sendDeliveryAssignment(deliveryPartner, order);
        notificationService.sendDeliveryPartnerAssigned(order, deliveryPartner);

        log.info("Delivery partner assigned successfully: assignmentId={}", assignment.getId());

        return mapToDeliveryAssignmentResponse(assignment);
    }

    public DeliveryAssignmentResponse acceptDelivery(Long assignmentId, Long deliveryPartnerId) {

        log.info("Accepting delivery: assignmentId={}, partnerId={}", assignmentId, deliveryPartnerId);

        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        validateDeliveryPartner(assignment, deliveryPartnerId);

        if (assignment.getStatus() != DeliveryAssignment.DeliveryStatus.ASSIGNED) {
            throw new BadRequestException("Cannot accept delivery in current status: " + assignment.getStatus());
        }

        assignment.setStatus(DeliveryAssignment.DeliveryStatus.ACCEPTED);
        assignment.setAcceptedAt(LocalDateTime.now());

        deliveryAssignmentRepository.save(assignment);

        timelineService.addEvent(assignment.getOrder(),
                OrderTimeline.EventType.DELIVERY_ASSIGNED,
                "Delivery partner on the way",
                "Your delivery partner is heading to the restaurant");

        notificationService.sendOrderUpdate(assignment.getOrder(),
                "Delivery partner accepted your order");

        log.info("Delivery accepted successfully");

        return mapToDeliveryAssignmentResponse(assignment);
    }

    public DeliveryAssignmentResponse markPickedUp(
            Long assignmentId, Long deliveryPartnerId, LocationUpdateRequest location) {

        log.info("Marking order as picked up: assignmentId={}", assignmentId);

        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        validateDeliveryPartner(assignment, deliveryPartnerId);

        if (assignment.getStatus() != DeliveryAssignment.DeliveryStatus.ACCEPTED) {
            throw new BadRequestException("Order must be accepted before pickup");
        }

        assignment.setStatus(DeliveryAssignment.DeliveryStatus.PICKED_UP);
        assignment.setPickedUpAt(LocalDateTime.now());

        if (location != null) {
            assignment.setCurrentLatitude(location.getLatitude());
            assignment.setCurrentLongitude(location.getLongitude());
            assignment.setLastLocationUpdate(LocalDateTime.now());
        }

        deliveryAssignmentRepository.save(assignment);

        Order order = assignment.getOrder();
        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(order);

        timelineService.addEvent(order, OrderTimeline.EventType.OUT_FOR_DELIVERY,
                "Order is on the way!",
                "Your delivery partner is heading to your location");

        notificationService.sendOrderOutForDelivery(order, assignment);

        log.info("Order marked as picked up");

        return mapToDeliveryAssignmentResponse(assignment);
    }

    public void updateLocation(Long assignmentId, Long deliveryPartnerId,
                               LocationUpdateRequest location) {

        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        validateDeliveryPartner(assignment, deliveryPartnerId);

        assignment.setCurrentLatitude(location.getLatitude());
        assignment.setCurrentLongitude(location.getLongitude());
        assignment.setLastLocationUpdate(LocalDateTime.now());

        if (location.getDistanceRemainingKm() != null) {
            assignment.setDistanceRemainingKm(BigDecimal.valueOf(location.getDistanceRemainingKm()));
        }

        deliveryAssignmentRepository.save(assignment);
    }

    public DeliveryAssignmentResponse markDelivered(
            Long assignmentId, Long deliveryPartnerId, DeliverOrderRequest request) {

        log.info("Marking order as delivered: assignmentId={}", assignmentId);

        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        validateDeliveryPartner(assignment, deliveryPartnerId);

        if (assignment.getStatus() != DeliveryAssignment.DeliveryStatus.PICKED_UP
                && assignment.getStatus() != DeliveryAssignment.DeliveryStatus.IN_TRANSIT) {
            throw new BadRequestException("Order must be picked up before marking as delivered");
        }

        assignment.setStatus(DeliveryAssignment.DeliveryStatus.DELIVERED);
        assignment.setDeliveredAt(LocalDateTime.now());
        assignment.setDeliveryNotes(request.getNotes());

        deliveryAssignmentRepository.save(assignment);

        Order order = assignment.getOrder();
        order.setStatus(Order.OrderStatus.DELIVERED);
        orderRepository.save(order);

        timelineService.addEvent(order, OrderTimeline.EventType.DELIVERED,
                "Order delivered!",
                "Your order has been delivered. Enjoy your meal!");

        notificationService.sendOrderDelivered(order);

        log.info("Order delivered successfully");

        return mapToDeliveryAssignmentResponse(assignment);
    }

    public List<DeliveryAssignmentResponse> getActiveDeliveriesForPartner(Long deliveryPartnerId) {

        List<DeliveryAssignment.DeliveryStatus> activeStatuses = Arrays.asList(
                DeliveryAssignment.DeliveryStatus.ASSIGNED,
                DeliveryAssignment.DeliveryStatus.ACCEPTED,
                DeliveryAssignment.DeliveryStatus.PICKED_UP,
                DeliveryAssignment.DeliveryStatus.IN_TRANSIT
        );

        return deliveryAssignmentRepository
                .findActiveDeliveriesForPartner(deliveryPartnerId, activeStatuses)
                .stream()
                .map(this::mapToDeliveryAssignmentResponse)
                .collect(Collectors.toList());
    }

    private LocalDateTime calculateEstimatedDeliveryTime(Order order) {
        return LocalDateTime.now().plusMinutes(30);
    }

    private void validateDeliveryPartner(DeliveryAssignment assignment, Long deliveryPartnerId) {
        if (!assignment.getDeliveryPartner().getId().equals(deliveryPartnerId)) {
            throw new UnauthorizedException("Not authorized for this delivery");
        }
    }

    private DeliveryOrderResponse mapToDeliveryOrderResponse(Order order) {
        return DeliveryOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer() != null
                        ? order.getCustomer().getFullName() : "Guest")
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .totalAmount(order.getTotalAmount())
                .readyTime(order.getActualReadyTime())
                .status(order.getStatus().name())
                .priority(order.getPriority())
                .build();
    }

    private DeliveryAssignmentResponse mapToDeliveryAssignmentResponse(DeliveryAssignment assignment) {
        return DeliveryAssignmentResponse.builder()
                .id(assignment.getId())
                .orderId(assignment.getOrder().getId())
                .orderNumber(assignment.getOrder().getOrderNumber())
                .deliveryPartnerName(assignment.getDeliveryPartner().getFullName())
                .deliveryPartnerPhone(assignment.getDeliveryPartner().getPhone())
                .status(assignment.getStatus().name())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .pickedUpAt(assignment.getPickedUpAt())
                .estimatedDeliveryTime(assignment.getEstimatedDeliveryTime())
                .customerAddress(assignment.getOrder().getDeliveryAddress())
                .customerPhone(assignment.getOrder().getCustomerPhone())
                .currentLatitude(assignment.getCurrentLatitude())
                .currentLongitude(assignment.getCurrentLongitude())
                .distanceRemainingKm(assignment.getDistanceRemainingKm())
                .deliveryNotes(assignment.getDeliveryNotes())
                .totalDeliveryTimeMinutes(assignment.getTotalDeliveryTimeMinutes())
                .build();
    }
}
