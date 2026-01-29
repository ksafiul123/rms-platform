package com.rms.service.kitchen;

// KitchenOrderService.java
//package com.rms.service.kitchen;

import com.rms.dto.kitchen.*;
import com.rms.dto.order.OrderDTO;
import com.rms.entity.*;
import com.rms.exception.*;
import com.rms.repository.*;
import com.rms.security.SecurityUtil;
import com.rms.service.notification.NotificationService;
import com.rms.service.tracking.OrderTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KitchenOrderService {

    private final OrderRepository orderRepository;
    private final KitchenOrderItemRepository kitchenOrderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderPreparationMetricsRepository metricsRepository;
    private final UserRepository userRepository;
    private final OrderTimelineService timelineService;
    private final NotificationService notificationService;

    public List<KitchenOrderResponse> getActiveKitchenOrders(
            Long restaurantId, String statusFilter, String stationFilter) {

        log.info("Fetching active kitchen orders for restaurant: {}", restaurantId);

        List<Order.OrderStatus> activeStatuses = Arrays.asList(
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PREPARING
        );

        List<Order> orders = orderRepository
                .findByRestaurantIdAndStatusInOrderByPriorityDescCreatedAtAsc(
                        restaurantId, activeStatuses);

        return orders.stream()
                .map(this::mapToKitchenOrderResponse)
                .collect(Collectors.toList());
    }

    public KitchenOrderResponse startPreparingOrder(
            Long orderId, Long userId, StartPreparationRequest request) {

        log.info("Starting order preparation: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        validateRestaurantAccess(order.getRestaurantId());

        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order must be in CONFIRMED status to start preparing");
        }

        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.PREPARING);

        Integer estimatedTime = request != null && request.getEstimatedTimeMinutes() != null
                ? request.getEstimatedTimeMinutes()
                : order.getEstimatedPreparationTimeMinutes();

        if (estimatedTime != null) {
            order.setEstimatedReadyTime(LocalDateTime.now().plusMinutes(estimatedTime));
        }

        if (order.getKitchenItems().isEmpty()) {
            createKitchenItems(order, request);
        }

        recordStatusChange(order, previousStatus, userId,
                request != null ? request.getNotes() : null);

        timelineService.addEvent(order, OrderTimeline.EventType.KITCHEN_STARTED,
                "Kitchen has started preparing your order",
                "Your delicious food is being prepared by our chefs");

        updateMetrics(order);

        orderRepository.save(order);

        notificationService.sendOrderStatusUpdate(order,
                "Your order #" + order.getOrderNumber() + " is being prepared");

        log.info("Order preparation started successfully: {}", orderId);

        return mapToKitchenOrderResponse(order);
    }

    public KitchenOrderItemResponse updateItemStatus(
            Long orderId, Long itemId, Long userId, UpdateItemStatusRequest request) {

        log.info("Updating kitchen item status: itemId={}, newStatus={}", itemId, request.getStatus());

        KitchenOrderItem kitchenItem = kitchenOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen item not found: " + itemId));

        if (!kitchenItem.getOrder().getId().equals(orderId)) {
            throw new BadRequestException("Item does not belong to this order");
        }

        validateRestaurantAccess(kitchenItem.getOrder().getRestaurantId());

        KitchenOrderItem.ItemStatus newStatus;
        try {
            newStatus = KitchenOrderItem.ItemStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }

        kitchenItem.setStatus(newStatus);
        kitchenItem.setPreparationNotes(request.getNotes());

        if (newStatus == KitchenOrderItem.ItemStatus.IN_PROGRESS
                && kitchenItem.getStartedAt() == null) {
            kitchenItem.setStartedAt(LocalDateTime.now());
        } else if (newStatus == KitchenOrderItem.ItemStatus.COMPLETED) {
            kitchenItem.setCompletedAt(LocalDateTime.now());
        }

        kitchenOrderItemRepository.save(kitchenItem);

        checkIfAllItemsCompleted(orderId);

        return mapToKitchenOrderItemResponse(kitchenItem);
    }

    public OrderResponse markOrderReady(Long orderId, Long userId, MarkReadyRequest request) {

        log.info("Marking order as ready: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        validateRestaurantAccess(order.getRestaurantId());

        if (order.getStatus() != Order.OrderStatus.PREPARING) {
            throw new BadRequestException("Order must be in PREPARING status");
        }

        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.READY);
        order.setActualReadyTime(LocalDateTime.now());

        recordStatusChange(order, previousStatus, userId,
                request != null ? request.getNotes() : null);

        String timelineDescription = order.getOrderType() == Order.OrderType.DINE_IN
                ? "Your food will be served shortly"
                : "Your order is ready for pickup";

        timelineService.addEvent(order, OrderTimeline.EventType.FOOD_READY,
                "Your order is ready!",
                timelineDescription);

        updateMetrics(order);

        orderRepository.save(order);

        if (request == null || request.getNotifyCustomer()) {
            notificationService.sendOrderReadyNotification(order);
        }

        log.info("Order marked as ready: {}", orderId);

        return mapToOrderResponse(order);
    }

    public KitchenMetricsResponse getKitchenMetrics(Long restaurantId, LocalDate date) {

        log.info("Fetching kitchen metrics: restaurantId={}, date={}", restaurantId, date);

        List<OrderPreparationMetrics> metrics =
                metricsRepository.findByRestaurantAndDate(restaurantId, date);

        if (metrics.isEmpty()) {
            return KitchenMetricsResponse.builder()
                    .date(date)
                    .totalOrders(0)
                    .build();
        }

        int totalOrders = metrics.size();
        long completedOrders = metrics.stream()
                .filter(m -> m.getReadyAt() != null)
                .count();

        double avgPrepTime = metrics.stream()
                .filter(m -> m.getActualPreparationTime() != null)
                .mapToInt(OrderPreparationMetrics::getActualPreparationTime)
                .average()
                .orElse(0.0);

        long ordersOnTime = metrics.stream()
                .filter(OrderPreparationMetrics::getWasOnTime)
                .count();

        double onTimePercentage = totalOrders > 0
                ? (ordersOnTime * 100.0) / totalOrders
                : 0.0;

        return KitchenMetricsResponse.builder()
                .date(date)
                .totalOrders(totalOrders)
                .completedOrders((int) completedOrders)
                .inProgressOrders(totalOrders - (int) completedOrders)
                .averagePreparationTime(avgPrepTime)
                .ordersOnTime((int) ordersOnTime)
                .ordersDelayed(totalOrders - (int) ordersOnTime)
                .onTimePercentage(onTimePercentage)
                .build();
    }

    private void createKitchenItems(Order order, StartPreparationRequest request) {

        Map<Long, StartPreparationRequest.ItemAssignment> assignmentMap = new HashMap<>();
        if (request != null && request.getItemAssignments() != null) {
            assignmentMap = request.getItemAssignments().stream()
                    .collect(Collectors.toMap(
                            StartPreparationRequest.ItemAssignment::getOrderItemId,
                            a -> a,
                            (a1, a2) -> a1
                    ));
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            KitchenOrderItem kitchenItem = KitchenOrderItem.builder()
                    .order(order)
                    .orderItem(orderItem)
                    .status(KitchenOrderItem.ItemStatus.NOT_STARTED)
                    .priority(order.getPriority())
                    .build();

            StartPreparationRequest.ItemAssignment assignment =
                    assignmentMap.get(orderItem.getId());

            if (assignment != null) {
                if (assignment.getAssignedChefId() != null) {
                    userRepository.findById(assignment.getAssignedChefId())
                            .ifPresent(kitchenItem::setAssignedChef);
                }
                kitchenItem.setStation(assignment.getStation());
            }

            kitchenOrderItemRepository.save(kitchenItem);
        }
    }

    private void checkIfAllItemsCompleted(Long orderId) {
        List<KitchenOrderItem> items = kitchenOrderItemRepository.findByOrderId(orderId);

        boolean allCompleted = items.stream()
                .allMatch(item -> item.getStatus() == KitchenOrderItem.ItemStatus.COMPLETED);

        if (allCompleted && !items.isEmpty()) {
            log.info("All items completed for order: {}, auto-marking as ready", orderId);

            try {
                Long currentUserId = SecurityUtil.getCurrentUserId();
                markOrderReady(orderId, currentUserId, null);
            } catch (Exception e) {
                log.error("Failed to auto-mark order as ready: {}", orderId, e);
            }
        }
    }

    private void recordStatusChange(Order order, Order.OrderStatus previousStatus,
                                    Long userId, String notes) {

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(order.getStatus())
                .previousStatus(previousStatus)
                .notes(notes)
                .timestamp(LocalDateTime.now())
                .build();

        userRepository.findById(userId).ifPresent(history::setUpdatedBy);

        statusHistoryRepository.save(history);
    }

    private void updateMetrics(Order order) {
        OrderPreparationMetrics metrics = metricsRepository
                .findByOrderId(order.getId())
                .orElseGet(() -> {
                    OrderPreparationMetrics newMetrics = new OrderPreparationMetrics();
                    newMetrics.setOrder(order);
                    newMetrics.setTargetPreparationTime(order.getEstimatedPreparationTimeMinutes());
                    newMetrics.setTotalItems(order.getOrderItems().size());
                    return newMetrics;
                });

        if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            metrics.setConfirmedAt(LocalDateTime.now());
        } else if (order.getStatus() == Order.OrderStatus.PREPARING) {
            metrics.setKitchenStartedAt(LocalDateTime.now());
        } else if (order.getStatus() == Order.OrderStatus.READY) {
            metrics.setKitchenCompletedAt(LocalDateTime.now());
            metrics.setReadyAt(LocalDateTime.now());
            metrics.calculateMetrics();
        }

        metricsRepository.save(metrics);
    }

    private void validateRestaurantAccess(Long restaurantId) {
        Long currentRestaurantId = SecurityUtil.getCurrentRestaurantId();
        if (!currentRestaurantId.equals(restaurantId)) {
            throw new UnauthorizedException("Access denied to this restaurant's orders");
        }
    }

    private KitchenOrderResponse mapToKitchenOrderResponse(Order order) {

        int elapsedMinutes = (int) Duration.between(
                order.getCreatedAt(), LocalDateTime.now()).toMinutes();

        List<KitchenOrderItem> kitchenItems =
                kitchenOrderItemRepository.findByOrderId(order.getId());

        long itemsPrepared = kitchenItems.stream()
                .filter(item -> item.getStatus() == KitchenOrderItem.ItemStatus.COMPLETED)
                .count();

        return KitchenOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType().name())
                .tableNumber(order.getTableNumber())
                .customerName(order.getCustomer() != null
                        ? order.getCustomer().getFullName() : "Guest")
                .status(order.getStatus().name())
                .priority(order.getPriority())
                .isRushOrder(order.getIsRushOrder())
                .orderTime(order.getCreatedAt())
                .estimatedReadyTime(order.getEstimatedReadyTime())
                .estimatedPreparationTimeMinutes(order.getEstimatedPreparationTimeMinutes())
                .elapsedTimeMinutes(elapsedMinutes)
                .items(kitchenItems.stream()
                        .map(this::mapToKitchenOrderItemResponse)
                        .collect(Collectors.toList()))
                .kitchenNotes(order.getKitchenNotes())
                .specialInstructions(order.getSpecialInstructions())
                .totalAmount(order.getTotalAmount())
                .totalItems(kitchenItems.size())
                .itemsPrepared((int) itemsPrepared)
                .build();
    }

    private KitchenOrderItemResponse mapToKitchenOrderItemResponse(KitchenOrderItem item) {

        List<String> modifiers = new ArrayList<>();
        // Add modifier mapping logic here

        return KitchenOrderItemResponse.builder()
                .id(item.getId())
                .orderItemId(item.getOrderItem().getId())
                .itemName(item.getOrderItem().getMenuItem().getName())
                .quantity(item.getOrderItem().getQuantity())
                .status(item.getStatus().name())
                .assignedChefName(item.getAssignedChef() != null
                        ? item.getAssignedChef().getFullName() : null)
                .station(item.getStation())
                .priority(item.getPriority())
                .startedAt(item.getStartedAt())
                .completedAt(item.getCompletedAt())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .modifiers(modifiers)
                .specialInstructions(item.getOrderItem().getSpecialInstructions())
                .preparationNotes(item.getPreparationNotes())
                .build();
    }

    private OrderDTO.OrderResponse mapToOrderResponse(Order order) {
        // Implement order response mapping
        return new OrderDTO.OrderResponse();
    }
}