package com.rms.service.tracking;

import com.rms.dto.tracking.DeliveryTrackingResponse;
import com.rms.dto.tracking.EstimatedTimeResponse;
import com.rms.dto.tracking.LiveOrderStatusResponse;
import com.rms.dto.tracking.OrderTimelineResponse;
import com.rms.entity.DeliveryAssignment;
import com.rms.entity.KitchenOrderItem;
import com.rms.entity.Order;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.DeliveryAssignmentRepository;
import com.rms.repository.KitchenOrderItemRepository;
import com.rms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.rms.entity.DeliveryAssignment.DeliveryStatus.DELIVERED;
import static com.rms.enums.OnboardingStatus.REJECTED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderTrackingService {

    private final OrderRepository orderRepository;
    private final KitchenOrderItemRepository kitchenOrderItemRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final OrderTimelineService timelineService;

    public LiveOrderStatusResponse getLiveOrderStatus(Long orderId, Long customerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("Not authorized to view this order");
        }

        List<KitchenOrderItem> kitchenItems =
                kitchenOrderItemRepository.findByOrderId(orderId);

        long itemsPrepared = kitchenItems.stream()
                .filter(item -> item.getStatus() == KitchenOrderItem.ItemStatus.COMPLETED)
                .count();

        DeliveryAssignment delivery = deliveryAssignmentRepository
                .findByOrderId(orderId).orElse(null);

        return LiveOrderStatusResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .currentStatus(order.getStatus().name())
                .currentStatusDisplay(getStatusDisplayText(order.getStatus()))
                .progressPercentage(calculateProgressPercentage(order.getStatus()))
                .orderTime(order.getCreatedAt())
                .estimatedReadyTime(order.getEstimatedReadyTime())
                .actualReadyTime(order.getActualReadyTime())
                .estimatedDeliveryTime(delivery != null ? delivery.getEstimatedDeliveryTime() : null)
                .remainingMinutes(calculateRemainingMinutes(order, delivery))
                .statusMessage(getStatusMessage(order))
                .nextStatus(getNextStatus(order.getStatus()))
                .totalItems(kitchenItems.size())
                .itemsPrepared((int) itemsPrepared)
                .itemsRemaining(kitchenItems.size() - (int) itemsPrepared)
                .deliveryPartnerName(delivery != null ? delivery.getDeliveryPartner().getFullName() : null)
                .deliveryPartnerPhone(delivery != null ? delivery.getDeliveryPartner().getPhone() : null)
                .deliveryStatus(delivery != null ? delivery.getStatus().name() : null)
                .deliveryLatitude(delivery != null ? delivery.getCurrentLatitude() : null)
                .deliveryLongitude(delivery != null ? delivery.getCurrentLongitude() : null)
                .distanceRemainingKm(delivery != null ? delivery.getDistanceRemainingKm() : null)
                .canCancel(canCancelOrder(order))
                .canTrackDelivery(delivery != null && delivery.getStatus() == DeliveryAssignment.DeliveryStatus.PICKED_UP)
                .build();
    }

    public List<OrderTimelineResponse> getOrderTimeline(Long orderId, Long customerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("Not authorized to view this order");
        }

        return timelineService.getTimeline(orderId);
    }

    public DeliveryTrackingResponse getDeliveryTracking(Long orderId, Long customerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("Not authorized to view this order");
        }

        DeliveryAssignment delivery = deliveryAssignmentRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery assignment found"));

        return DeliveryTrackingResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .deliveryPartnerName(delivery.getDeliveryPartner().getFullName())
                .deliveryPartnerPhone(delivery.getDeliveryPartner().getPhone())
                .currentLatitude(delivery.getCurrentLatitude())
                .currentLongitude(delivery.getCurrentLongitude())
                .distanceRemainingKm(delivery.getDistanceRemainingKm())
                .estimatedMinutesRemaining(calculateDeliveryETA(delivery))
                .lastLocationUpdate(delivery.getLastLocationUpdate())
                .deliveryStatus(delivery.getStatus().name())
                .build();
    }

    public EstimatedTimeResponse getEstimatedTime(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        DeliveryAssignment delivery = deliveryAssignmentRepository
                .findByOrderId(orderId).orElse(null);

        Integer remainingMinutes = calculateRemainingMinutes(order, delivery);

        return EstimatedTimeResponse.builder()
                .orderId(order.getId())
                .estimatedReadyTime(order.getEstimatedReadyTime())
                .estimatedDeliveryTime(delivery != null ? delivery.getEstimatedDeliveryTime() : null)
                .estimatedMinutesRemaining(remainingMinutes)
                .message(remainingMinutes != null
                        ? "Your order will be ready in approximately " + remainingMinutes + " minutes"
                        : "Preparing your order")
                .build();
    }

    private String getStatusDisplayText(Order.OrderStatus status) {
        return switch (status) {
            case PLACED -> "Order Placed";
            case CONFIRMED -> "Order Confirmed";
            case PREPARING -> "Preparing Your Food";
            case READY -> "Order Ready";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
            case REJECTED -> "Rejected";
        };
    }

    private int calculateProgressPercentage(Order.OrderStatus status) {
        return switch (status) {
            case PLACED -> 10;
            case CONFIRMED -> 25;
            case PREPARING -> 50;
            case READY -> 75;
            case OUT_FOR_DELIVERY -> 90;
            case DELIVERED -> 100;
            case CANCELLED, REJECTED -> 0;
        };
    }

    private String getStatusMessage(Order order) {
        return switch (order.getStatus()) {
            case PLACED -> "We've received your order and are reviewing it";
            case CONFIRMED -> "Your order has been confirmed and will be prepared soon";
            case PREPARING -> "Our chefs are preparing your delicious food";
            case READY -> order.getOrderType() == Order.OrderType.DINE_IN
                    ? "Your food will be served shortly"
                    : "Your order is ready for pickup";
            case OUT_FOR_DELIVERY -> "Your order is on the way to you";
            case DELIVERED -> "Enjoy your meal!";
            case CANCELLED -> "This order has been cancelled";
            case REJECTED -> "We're sorry, we couldn't accept this order";
        };
    }

    private String getNextStatus(Order.OrderStatus currentStatus) {
        return switch (currentStatus) {
            case PLACED -> "CONFIRMED";
            case CONFIRMED -> "PREPARING";
            case PREPARING -> "READY";
            case READY -> "OUT_FOR_DELIVERY";
            case OUT_FOR_DELIVERY -> "DELIVERED";
            default -> null;
        };
    }

    private Integer calculateRemainingMinutes(Order order, DeliveryAssignment delivery) {

        if (order.getStatus() == Order.OrderStatus.DELIVERED
                || order.getStatus() == Order.OrderStatus.CANCELLED) {
            return null;
        }

        LocalDateTime targetTime;

        if (order.getStatus() == Order.OrderStatus.OUT_FOR_DELIVERY && delivery != null) {
            targetTime = delivery.getEstimatedDeliveryTime();
        } else {
            targetTime = order.getEstimatedReadyTime();
        }

        if (targetTime == null) {
            return null;
        }

        long minutes = Duration.between(LocalDateTime.now(), targetTime).toMinutes();
        return minutes > 0 ? (int) minutes : 0;
    }

    private Integer calculateDeliveryETA(DeliveryAssignment delivery) {
        if (delivery.getEstimatedDeliveryTime() == null) {
            return null;
        }

        long minutes = Duration.between(
                LocalDateTime.now(),
                delivery.getEstimatedDeliveryTime()
        ).toMinutes();

        return minutes > 0 ? (int) minutes : 0;
    }

    private boolean canCancelOrder(Order order) {
        return order.getStatus() == Order.OrderStatus.PLACED
                || order.getStatus() == Order.OrderStatus.CONFIRMED;
    }
}
