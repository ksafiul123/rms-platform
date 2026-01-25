package com.rms.validation;

import com.rms.entity.Order;
import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * Order validation and business rules
 */
@UtilityClass
public class OrderValidationRules {

    /**
     * Valid order status transitions
     */
    public static final Map<Order.OrderStatus, Set<Order.OrderStatus>> VALID_STATUS_TRANSITIONS = Map.of(
            Order.OrderStatus.PENDING, Set.of(
                    Order.OrderStatus.CONFIRMED,
                    Order.OrderStatus.CANCELLED
            ),
            Order.OrderStatus.CONFIRMED, Set.of(
                    Order.OrderStatus.PREPARING,
                    Order.OrderStatus.CANCELLED
            ),
            Order.OrderStatus.PREPARING, Set.of(
                    Order.OrderStatus.READY,
                    Order.OrderStatus.CANCELLED
            ),
            Order.OrderStatus.READY, Set.of(
                    Order.OrderStatus.OUT_FOR_DELIVERY,
                    Order.OrderStatus.COMPLETED
            ),
            Order.OrderStatus.OUT_FOR_DELIVERY, Set.of(
                    Order.OrderStatus.COMPLETED
            )
    );

    /**
     * Roles allowed to perform status transitions
     */
    public static final Map<Order.OrderStatus, Set<String>> STATUS_UPDATE_ROLES = Map.of(
            Order.OrderStatus.CONFIRMED, Set.of("RESTAURANT_ADMIN", "ADMIN"),
            Order.OrderStatus.PREPARING, Set.of("CHEF", "RESTAURANT_ADMIN", "ADMIN"),
            Order.OrderStatus.READY, Set.of("CHEF", "RESTAURANT_ADMIN", "ADMIN"),
            Order.OrderStatus.OUT_FOR_DELIVERY, Set.of("DELIVERY_MAN", "RESTAURANT_ADMIN", "ADMIN"),
            Order.OrderStatus.COMPLETED, Set.of("DELIVERY_MAN", "RESTAURANT_ADMIN", "ADMIN"),
            Order.OrderStatus.CANCELLED, Set.of("CUSTOMER", "RESTAURANT_ADMIN", "ADMIN")
    );

    /**
     * Order types that require specific fields
     */
    public static final Map<Order.OrderType, Set<String>> REQUIRED_FIELDS = Map.of(
            Order.OrderType.DINE_IN, Set.of("tableNumber"),
            Order.OrderType.DELIVERY, Set.of("deliveryAddress"),
            Order.OrderType.TAKEAWAY, Set.of()
    );

    /**
     * Cancellable statuses (orders in these statuses can be cancelled)
     */
    public static final Set<Order.OrderStatus> CANCELLABLE_STATUSES = Set.of(
            Order.OrderStatus.PENDING,
            Order.OrderStatus.CONFIRMED,
            Order.OrderStatus.PREPARING
    );

    /**
     * Customer can only cancel orders in these statuses
     */
    public static final Set<Order.OrderStatus> CUSTOMER_CANCELLABLE_STATUSES = Set.of(
            Order.OrderStatus.PENDING
    );

    /**
     * Statuses where order can be modified
     */
    public static final Set<Order.OrderStatus> MODIFIABLE_STATUSES = Set.of(
            Order.OrderStatus.PENDING
    );

    /**
     * Terminal statuses (no further transitions allowed)
     */
    public static final Set<Order.OrderStatus> TERMINAL_STATUSES = Set.of(
            Order.OrderStatus.COMPLETED,
            Order.OrderStatus.CANCELLED
    );

    /**
     * Active statuses (orders that need attention)
     */
    public static final Set<Order.OrderStatus> ACTIVE_STATUSES = Set.of(
            Order.OrderStatus.PENDING,
            Order.OrderStatus.CONFIRMED,
            Order.OrderStatus.PREPARING,
            Order.OrderStatus.READY,
            Order.OrderStatus.OUT_FOR_DELIVERY
    );

    /**
     * Check if status transition is valid
     */
    public static boolean isValidTransition(Order.OrderStatus from, Order.OrderStatus to) {
        if (TERMINAL_STATUSES.contains(from)) {
            return false;
        }
        return VALID_STATUS_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    /**
     * Check if role can update to specific status
     */
    public static boolean canRoleUpdateToStatus(String role, Order.OrderStatus status) {
        return STATUS_UPDATE_ROLES.getOrDefault(status, Set.of()).contains(role);
    }

    /**
     * Check if order can be cancelled
     */
    public static boolean isCancellable(Order.OrderStatus status) {
        return CANCELLABLE_STATUSES.contains(status);
    }

    /**
     * Check if customer can cancel order in this status
     */
    public static boolean isCustomerCancellable(Order.OrderStatus status) {
        return CUSTOMER_CANCELLABLE_STATUSES.contains(status);
    }

    /**
     * Check if order can be modified
     */
    public static boolean isModifiable(Order.OrderStatus status) {
        return MODIFIABLE_STATUSES.contains(status);
    }

    /**
     * Check if status is terminal
     */
    public static boolean isTerminal(Order.OrderStatus status) {
        return TERMINAL_STATUSES.contains(status);
    }

    /**
     * Check if status is active
     */
    public static boolean isActive(Order.OrderStatus status) {
        return ACTIVE_STATUSES.contains(status);
    }

    /**
     * Get next possible statuses from current status
     */
    public static Set<Order.OrderStatus> getNextStatuses(Order.OrderStatus current) {
        return VALID_STATUS_TRANSITIONS.getOrDefault(current, Set.of());
    }

    /**
     * Get required fields for order type
     */
    public static Set<String> getRequiredFields(Order.OrderType orderType) {
        return REQUIRED_FIELDS.getOrDefault(orderType, Set.of());
    }
}
