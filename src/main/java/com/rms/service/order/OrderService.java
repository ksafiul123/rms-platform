package com.rms.service.order;

//package com.rms.service;

import com.rms.dto.order.OrderDTO.*;
import com.rms.entity.*;
import com.rms.entity.*;
import com.rms.exception.BadRequestException;
import com.rms.exception.ForbiddenException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.OrderRepository;
import com.rms.repository.UserRepository;
import com.rms.security.UserPrincipal;
import com.rms.service.menu.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuService menuService;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("5.00");
    private static final int PREPARATION_TIME_MINUTES = 30;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UserPrincipal currentUser) {
        log.info("Creating order for user: {} in restaurant: {}",
                currentUser.getId(), currentUser.getRestaurantId());

        // Validate order type specific requirements
        validateOrderTypeRequirements(request);

        // Validate customer role
        if (!currentUser.hasRole("CUSTOMER")) {
            throw new ForbiddenException("Only customers can create orders");
        }

        Order order = new Order();
        order.setRestaurantId(currentUser.getRestaurantId());
        order.setCustomerId(currentUser.getId());
        order.setOrderNumber(generateOrderNumber());
        order.setOrderType(request.getOrderType());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTableNumber(request.getTableNumber());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setDiscountAmount(request.getDiscountAmount() != null ?
                request.getDiscountAmount() : BigDecimal.ZERO);

        // Process order items
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = createOrderItem(itemRequest, currentUser.getRestaurantId());
            order.addOrderItem(orderItem);
            subtotal = subtotal.add(orderItem.getSubtotal());
        }

        // Calculate totals
        order.setSubtotal(subtotal);
        order.setTaxAmount(subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP));

        if (request.getOrderType() == Order.OrderType.DELIVERY) {
            order.setDeliveryFee(DELIVERY_FEE);
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
        }

        BigDecimal total = order.getSubtotal()
                .add(order.getTaxAmount())
                .add(order.getDeliveryFee())
                .subtract(order.getDiscountAmount());
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        // Set estimated ready time
        order.setEstimatedReadyTime(LocalDateTime.now().plusMinutes(PREPARATION_TIME_MINUTES));

        // Add initial status history
        OrderStatusHistory history = new OrderStatusHistory();
        history.setFromStatus(null);
        history.setToStatus(Order.OrderStatus.PENDING);
        history.setChangedBy(currentUser.getId());
        history.setNotes("Order created");
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());

        return mapToOrderResponse(savedOrder);
    }

    private void validateOrderTypeRequirements(CreateOrderRequest request) {
        if (request.getOrderType() == Order.OrderType.DINE_IN) {
            if (request.getTableNumber() == null || request.getTableNumber().isBlank()) {
                throw new BadRequestException("Table number is required for dine-in orders");
            }
        } else if (request.getOrderType() == Order.OrderType.DELIVERY) {
            if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
                throw new BadRequestException("Delivery address is required for delivery orders");
            }
        }
    }

    private OrderItem createOrderItem(OrderItemRequest request, Long restaurantId) {
        // Get menu item (this would call MenuService to get item details)
        // For now, simulating with placeholder data
        // In production, fetch actual menu item from MenuService

        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItemId(request.getMenuItemId());
        orderItem.setItemName("Menu Item " + request.getMenuItemId()); // Placeholder
        orderItem.setQuantity(request.getQuantity());
        orderItem.setUnitPrice(new BigDecimal("10.00")); // Placeholder
        orderItem.setSpecialInstructions(request.getSpecialInstructions());

        // Process modifiers
        if (request.getModifierIds() != null && !request.getModifierIds().isEmpty()) {
            for (Long modifierId : request.getModifierIds()) {
                OrderItemModifier modifier = new OrderItemModifier();
                modifier.setModifierId(modifierId);
                modifier.setModifierName("Modifier " + modifierId); // Placeholder
                modifier.setPrice(new BigDecimal("2.00")); // Placeholder
                orderItem.addModifier(modifier);
            }
        }

        orderItem.calculateSubtotal();
        return orderItem;
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(new Random().nextInt(1000));
        return "ORD" + timestamp.substring(timestamp.length() - 8) + random;
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request,
                                           UserPrincipal currentUser) {
        log.info("Updating order {} status to {}", orderId, request.getStatus());

        Order order = findOrderWithAccess(orderId, currentUser);

        // Validate status transition
        validateStatusTransition(order.getStatus(), request.getStatus());

        // Role-based status update validation
        validateRoleCanUpdateStatus(currentUser, request.getStatus());

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        // Update timestamps based on status
        switch (request.getStatus()) {
            case READY:
                order.setActualReadyTime(LocalDateTime.now());
                break;
            case COMPLETED:
                if (order.getOrderType() == Order.OrderType.DELIVERY) {
                    order.setDeliveryTime(LocalDateTime.now());
                }
                break;
        }

        // Add status history
        OrderStatusHistory history = new OrderStatusHistory();
        history.setFromStatus(oldStatus);
        history.setToStatus(request.getStatus());
        history.setChangedBy(currentUser.getId());
        history.setNotes(request.getNotes());
        order.addStatusHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated from {} to {}",
                orderId, oldStatus, request.getStatus());

        return mapToOrderResponse(updatedOrder);
    }

    private void validateStatusTransition(Order.OrderStatus from, Order.OrderStatus to) {
        if (from == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot update status of cancelled order");
        }
        if (from == Order.OrderStatus.COMPLETED) {
            throw new BadRequestException("Cannot update status of completed order");
        }

        // Define valid transitions
        Map<Order.OrderStatus, List<Order.OrderStatus>> validTransitions = Map.of(
                Order.OrderStatus.PENDING, List.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.CANCELLED),
                Order.OrderStatus.CONFIRMED, List.of(Order.OrderStatus.PREPARING, Order.OrderStatus.CANCELLED),
                Order.OrderStatus.PREPARING, List.of(Order.OrderStatus.READY, Order.OrderStatus.CANCELLED),
                Order.OrderStatus.READY, List.of(Order.OrderStatus.OUT_FOR_DELIVERY, Order.OrderStatus.COMPLETED),
                Order.OrderStatus.OUT_FOR_DELIVERY, List.of(Order.OrderStatus.COMPLETED)
        );

        if (!validTransitions.getOrDefault(from, Collections.emptyList()).contains(to)) {
            throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s", from, to));
        }
    }

    private void validateRoleCanUpdateStatus(UserPrincipal user, Order.OrderStatus newStatus) {
        if (newStatus == Order.OrderStatus.CONFIRMED &&
                !user.hasAnyRole("RESTAURANT_ADMIN", "ADMIN")) {
            throw new ForbiddenException("Only restaurant admin can confirm orders");
        }
        if ((newStatus == Order.OrderStatus.PREPARING || newStatus == Order.OrderStatus.READY) &&
                !user.hasAnyRole("CHEF", "RESTAURANT_ADMIN", "ADMIN")) {
            throw new ForbiddenException("Only chef can update preparation status");
        }
        if ((newStatus == Order.OrderStatus.OUT_FOR_DELIVERY ||
                (newStatus == Order.OrderStatus.COMPLETED && user.hasRole("DELIVERY_MAN"))) &&
                !user.hasAnyRole("DELIVERY_MAN", "RESTAURANT_ADMIN", "ADMIN")) {
            throw new ForbiddenException("Only delivery man can update delivery status");
        }
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request,
                                     UserPrincipal currentUser) {
        log.info("Cancelling order {} by user {}", orderId, currentUser.getId());

        Order order = findOrderWithAccess(orderId, currentUser);

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }
        if (order.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel completed order");
        }

        // Customers can only cancel PENDING orders
        if (currentUser.hasRole("CUSTOMER") &&
                order.getStatus() != Order.OrderStatus.PENDING) {
            throw new ForbiddenException("Customers can only cancel pending orders");
        }

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy(currentUser.getId());
        order.setCancellationReason(request.getReason());

        // Add status history
        OrderStatusHistory history = new OrderStatusHistory();
        history.setFromStatus(oldStatus);
        history.setToStatus(Order.OrderStatus.CANCELLED);
        history.setChangedBy(currentUser.getId());
        history.setNotes("Cancelled: " + request.getReason());
        order.addStatusHistory(history);

        Order cancelledOrder = orderRepository.save(order);
        log.info("Order {} cancelled successfully", orderId);

        return mapToOrderResponse(cancelledOrder);
    }

    @Transactional
    public OrderResponse assignDeliveryMan(Long orderId, AssignDeliveryManRequest request,
                                           UserPrincipal currentUser) {
        log.info("Assigning delivery man {} to order {}", request.getDeliveryManId(), orderId);

        if (!currentUser.hasAnyRole("RESTAURANT_ADMIN", "ADMIN")) {
            throw new ForbiddenException("Only restaurant admin can assign delivery man");
        }

        Order order = findOrderByIdAndRestaurantId(orderId, currentUser.getRestaurantId());

        if (order.getOrderType() != Order.OrderType.DELIVERY) {
            throw new BadRequestException("Can only assign delivery man to delivery orders");
        }

        if (order.getStatus() != Order.OrderStatus.READY &&
                order.getStatus() != Order.OrderStatus.OUT_FOR_DELIVERY) {
            throw new BadRequestException("Order must be ready before assigning delivery man");
        }

        // Verify delivery man exists and belongs to restaurant
        User deliveryMan = userRepository.findById(request.getDeliveryManId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery man not found"));

        if (!deliveryMan.getRoles().stream()
                .anyMatch(role -> role.getName().equals("DELIVERY_MAN"))) {
            throw new BadRequestException("User is not a delivery man");
        }

        order.setDeliveryManId(request.getDeliveryManId());
        Order updatedOrder = orderRepository.save(order);

        log.info("Delivery man assigned successfully to order {}", orderId);
        return mapToOrderResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, UserPrincipal currentUser) {
        Order order = findOrderWithAccess(orderId, currentUser);
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrders(UserPrincipal currentUser,
                                                Order.OrderStatus status,
                                                Order.OrderType orderType,
                                                Pageable pageable) {
        Page<Order> orders;

        if (currentUser.hasRole("CUSTOMER")) {
            // Customers see only their orders
            orders = orderRepository.findByCustomerIdAndRestaurantId(
                    currentUser.getId(), currentUser.getRestaurantId(), pageable);
        } else if (currentUser.hasRole("DELIVERY_MAN")) {
            // Delivery men see orders assigned to them
            orders = orderRepository.findByDeliveryManId(currentUser.getId(), pageable);
        } else {
            // Restaurant staff see all restaurant orders
            if (status != null) {
                orders = orderRepository.findByRestaurantIdAndStatus(
                        currentUser.getRestaurantId(), status, pageable);
            } else if (orderType != null) {
                orders = orderRepository.findByRestaurantIdAndOrderType(
                        currentUser.getRestaurantId(), orderType, pageable);
            } else {
                orders = orderRepository.findByRestaurantId(
                        currentUser.getRestaurantId(), pageable);
            }
        }

        return orders.map(this::mapToOrderSummaryResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getOrderHistory(Long orderId, UserPrincipal currentUser) {
        Order order = findOrderWithAccess(orderId, currentUser);

        return order.getStatusHistory().stream()
                .map(this::mapToOrderStatusHistoryResponse)
                .collect(Collectors.toList());
    }

    private Order findOrderWithAccess(Long orderId, UserPrincipal currentUser) {
        Order order = findOrderByIdAndRestaurantId(orderId, currentUser.getRestaurantId());

        // Customers can only access their own orders
        if (currentUser.hasRole("CUSTOMER") && !order.getCustomerId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied to this order");
        }

        // Delivery men can only access orders assigned to them
        if (currentUser.hasRole("DELIVERY_MAN") &&
                !currentUser.getId().equals(order.getDeliveryManId())) {
            throw new ForbiddenException("Access denied to this order");
        }

        return order;
    }

    private Order findOrderByIdAndRestaurantId(Long orderId, Long restaurantId) {
        return orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
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

        response.setItems(order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList()));

        return response;
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setMenuItemId(item.getMenuItemId());
        response.setItemName(item.getItemName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubtotal(item.getSubtotal());
        response.setSpecialInstructions(item.getSpecialInstructions());

        response.setModifiers(item.getModifiers().stream()
                .map(this::mapToOrderItemModifierResponse)
                .collect(Collectors.toList()));

        return response;
    }

    private OrderItemModifierResponse mapToOrderItemModifierResponse(OrderItemModifier modifier) {
        OrderItemModifierResponse response = new OrderItemModifierResponse();
        response.setId(modifier.getId());
        response.setModifierId(modifier.getModifierId());
        response.setModifierName(modifier.getModifierName());
        response.setPrice(modifier.getPrice());
        return response;
    }

    private OrderSummaryResponse mapToOrderSummaryResponse(Order order) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setOrderType(order.getOrderType());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setItemCount(order.getOrderItems().size());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    private OrderStatusHistoryResponse mapToOrderStatusHistoryResponse(OrderStatusHistory history) {
        OrderStatusHistoryResponse response = new OrderStatusHistoryResponse();
        response.setId(history.getId());
        response.setFromStatus(history.getFromStatus());
        response.setToStatus(history.getToStatus());
        response.setNotes(history.getNotes());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }

    /**
     * Validate order modifications for session orders
     *
     * Usage: Call this in OrderService.updateOrderStatus
     */
    public void validateSessionOrderUpdate(Order order, UserPrincipal currentUser) {
        if (order.getTableSession() != null) {
            TableSession session = order.getTableSession();

            // Session must be active for new orders or modifications
            if (session.getStatus() != TableSession.SessionStatus.ACTIVE &&
                    order.getStatus() == Order.OrderStatus.PENDING) {
                throw new BadRequestException("Cannot create orders in inactive session");
            }

            // Verify user is part of session for customer operations
            if (currentUser.hasRole("CUSTOMER")) {
                boolean isGuest = session.getGuests().stream()
                        .anyMatch(g -> g.getUserId() != null &&
                                g.getUserId().equals(currentUser.getId()) &&
                                g.getStatus() == TableSessionGuest.GuestStatus.ACTIVE);

                if (!isGuest) {
                    throw new ForbiddenException("You are not part of this session");
                }
            }
        }
    }

    /**
     * Update session total when order is updated
     *
     * Usage: Call this in OrderService after order updates
     */
    public void updateSessionTotal(Order order) {
        if (order.getTableSession() != null) {
            TableSession session = order.getTableSession();
            session.recalculateTotalAmount();
            sessionRepository.save(session);
            log.info("Session {} total updated to {}",
                    session.getId(), session.getTotalAmount());
        }
    }
}
}
