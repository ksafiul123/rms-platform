package com.rms.service;

//package com.rms.service;

import com.rms.dto.PreferenceDTO;
import com.rms.entity.Order;
import com.rms.entity.OrderItem;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Integration component to automatically track favorites and apply preferences to orders
 *
 * Add this logic to OrderService methods
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPreferenceIntegration {

    private final CustomerPreferenceService preferenceService;

    /**
     * Call this method after order is successfully completed
     * Updates favorite order counts for items in the order
     *
     * Example usage in OrderService:
     *
     * @Transactional
     * public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request,
     *                                       UserPrincipal currentUser) {
     *     // ... existing code ...
     *
     *     if (request.getStatus() == Order.OrderStatus.COMPLETED &&
     *         oldStatus != Order.OrderStatus.COMPLETED) {
     *         // Update favorite counts
     *         updateFavoriteCounts(order);
     *     }
     *
     *     // ... rest of method ...
     * }
     */
    public void updateFavoriteCounts(Order order) {
        log.info("Updating favorite counts for order {}", order.getOrderNumber());

        for (OrderItem item : order.getOrderItems()) {
            try {
                preferenceService.incrementFavoriteOrderCount(
                        order.getCustomerId(),
                        item.getMenuItemId()
                );
            } catch (Exception e) {
                log.warn("Failed to update favorite count for menu item {}: {}",
                        item.getMenuItemId(), e.getMessage());
                // Don't fail the order if favorite update fails
            }
        }
    }

    /**
     * Get customer preferences to display in kitchen/chef view
     *
     * Example usage in OrderController or KitchenController:
     *
     * @GetMapping("/orders/{orderId}/preferences")
     * @PreAuthorize("hasAnyRole('CHEF', 'RESTAURANT_ADMIN')")
     * public ResponseEntity<ApiResponse<OrderPreferencesSummary>> getOrderPreferences(
     *         @PathVariable Long orderId,
     *         @CurrentUser UserPrincipal currentUser) {
     *
     *     Order order = orderService.getOrderByIdInternal(orderId, currentUser.getRestaurantId());
     *     OrderPreferencesSummary summary = preferenceService.getOrderPreferences(
     *         order, currentUser);
     *
     *     return ResponseEntity.ok(
     *         ApiResponse.success("Order preferences retrieved", summary));
     * }
     */
    public PreferenceDTO.OrderPreferencesSummary getOrderPreferencesForKitchen(Order order,
                                                                               UserPrincipal currentUser) {
        return preferenceService.getOrderPreferences(order, currentUser);
    }
}

/**
 * IMPORTANT: Integration Points in Existing Services
 *
 * 1. OrderService.updateOrderStatus():
 *    - When status changes to COMPLETED, call updateFavoriteCounts(order)
 *
 * 2. OrderController - Add new endpoint:
 *    @GetMapping("/orders/{orderId}/preferences")
 *    @PreAuthorize("hasAnyRole('CHEF', 'RESTAURANT_ADMIN', 'ADMIN')")
 *    public ResponseEntity<ApiResponse<OrderPreferencesSummary>> getOrderPreferences(
 *            @PathVariable Long orderId,
 *            @CurrentUser UserPrincipal currentUser) {
 *
 *        Order order = orderService.findOrderById(orderId, currentUser);
 *        OrderPreferencesSummary summary = preferenceService.getOrderPreferences(
 *            order, currentUser);
 *
 *        return ResponseEntity.ok(
 *            ApiResponse.success("Order preferences retrieved successfully", summary));
 *    }
 *
 * 3. Create KitchenController for chef-specific views:
 *    @RestController
 *    @RequestMapping("/api/kitchen")
 *    @PreAuthorize("hasAnyRole('CHEF', 'RESTAURANT_ADMIN', 'ADMIN')")
 *    public class KitchenController {
 *
 *        @GetMapping("/orders/active")
 *        public ResponseEntity<...> getActiveOrders() {
 *            // Get orders with CONFIRMED, PREPARING status
 *            // Include customer preferences for each order
 *        }
 *
 *        @GetMapping("/orders/{orderId}/customer-preferences")
 *        public ResponseEntity<ApiResponse<ChefViewCustomerPreferenceResponse>>
 *                getCustomerPreferences(@PathVariable Long orderId) {
 *            // Get customer preferences for specific order
 *        }
 *    }
 */
