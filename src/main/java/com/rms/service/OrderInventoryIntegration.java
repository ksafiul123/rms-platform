package com.rms.service;

//package com.rms.service;

import com.rms.dto.InventoryDTO;
import com.rms.entity.Order;
import com.rms.exception.BadRequestException;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Integration component for automatic stock deduction on order confirmation
 *
 * Add this logic to OrderService.updateOrderStatus method:
 *
 * When order status changes to CONFIRMED, automatically deduct stock
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderInventoryIntegration {

    private final InventoryService inventoryService;

    /**
     * Call this method when order status changes to CONFIRMED
     *
     * Example usage in OrderService:
     *
     * @Transactional
     * public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request,
     *                                       UserPrincipal currentUser) {
     *     Order order = findOrderWithAccess(orderId, currentUser);
     *
     *     // Validate status transition
     *     validateStatusTransition(order.getStatus(), request.getStatus());
     *
     *     Order.OrderStatus oldStatus = order.getStatus();
     *     order.setStatus(request.getStatus());
     *
     *     // AUTOMATIC STOCK DEDUCTION
     *     if (request.getStatus() == Order.OrderStatus.CONFIRMED &&
     *         oldStatus != Order.OrderStatus.CONFIRMED) {
     *         try {
     *             inventoryService.deductStockForOrder(order, currentUser);
     *         } catch (InsufficientStockException e) {
     *             // Cannot confirm order due to insufficient stock
     *             throw new BadRequestException("Cannot confirm order: " + e.getMessage());
     *         }
     *     }
     *
     *     // ... rest of the method
     * }
     */
    public void handleOrderConfirmation(Order order, UserPrincipal currentUser) {
        log.info("Handling inventory deduction for order confirmation: {}",
                order.getOrderNumber());

        inventoryService.deductStockForOrder(order, currentUser);

        log.info("Inventory successfully deducted for order: {}", order.getOrderNumber());
    }

    /**
     * Check if order can be fulfilled before placing
     *
     * Example usage in OrderService.createOrder:
     *
     * @Transactional
     * public OrderResponse createOrder(CreateOrderRequest request, UserPrincipal currentUser) {
     *     // ... create order items ...
     *
     *     // CHECK AVAILABILITY BEFORE CREATING ORDER
     *     for (OrderItemRequest itemRequest : request.getItems()) {
     *         StockAvailabilityResponse availability = inventoryService
     *             .checkMenuItemAvailability(itemRequest.getMenuItemId(), itemRequest.getQuantity());
     *
     *         if (!availability.getIsAvailable()) {
     *             throw new BadRequestException(
     *                 String.format("Menu item '%s' is not available. Insufficient stock.",
     *                     availability.getMenuItemName()));
     *         }
     *     }
     *
     *     // ... continue with order creation ...
     * }
     */
    public void validateOrderAvailability(Order order) {
        log.info("Validating stock availability for order items");

        order.getOrderItems().forEach(orderItem -> {
            InventoryDTO.StockAvailabilityResponse availability = inventoryService
                    .checkMenuItemAvailability(orderItem.getMenuItemId(), orderItem.getQuantity());

            if (!availability.getIsAvailable()) {
                throw new BadRequestException(
                        String.format("Insufficient stock for %s", orderItem.getItemName()));
            }
        });

        log.info("All order items have sufficient stock");
    }
}

/**
 * IMPORTANT: Update OrderService with these integrations
 *
 * 1. Add field:
 *    private final InventoryService inventoryService;
 *
 * 2. In updateOrderStatus method, add after status change:
 *    // Automatic stock deduction on order confirmation
 *    if (request.getStatus() == Order.OrderStatus.CONFIRMED &&
 *        oldStatus != Order.OrderStatus.CONFIRMED) {
 *        try {
 *            inventoryService.deductStockForOrder(order, currentUser);
 *        } catch (InsufficientStockException e) {
 *            throw new BadRequestException("Cannot confirm order: " + e.getMessage());
 *        }
 *    }
 *
 * 3. Optionally, in createOrder method, add before creating order:
 *    // Check stock availability before creating order
 *    for (OrderItemRequest itemRequest : request.getItems()) {
 *        StockAvailabilityResponse availability = inventoryService
 *            .checkMenuItemAvailability(itemRequest.getMenuItemId(), itemRequest.getQuantity());
 *        if (!availability.getIsAvailable()) {
 *            throw new BadRequestException(
 *                String.format("Menu item is not available. Insufficient stock."));
 *        }
 *    }
 */
