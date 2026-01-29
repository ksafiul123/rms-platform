package com.rms.controller;

//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.order.OrderDTO.OrderResponse;
import com.rms.dto.PreferenceDTO.*;
import com.rms.entity.Order;
import com.rms.security.CurrentUser;
import com.rms.security.UserPrincipal;
import com.rms.service.CustomerPreferenceService;
import com.rms.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kitchen/Chef specific controller for viewing orders with customer preferences
 */
@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CHEF', 'RESTAURANT_ADMIN', 'ADMIN')")
public class KitchenController {

    private final OrderService orderService;
    private final CustomerPreferenceService preferenceService;

    @GetMapping("/orders/active")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getActiveOrders(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        // Get orders that need kitchen attention (CONFIRMED, PREPARING)
        Page<OrderResponse> orders = orderService.getOrders(
                currentUser, null, null, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Active kitchen orders retrieved successfully", orders));
    }

    @GetMapping("/orders/{orderId}/preferences")
    public ResponseEntity<ApiResponse<OrderPreferencesSummary>> getOrderPreferences(
            @PathVariable Long orderId,
            @CurrentUser UserPrincipal currentUser) {

        Order order = orderService.findOrderByIdInternal(orderId, currentUser.getRestaurantId());
        OrderPreferencesSummary summary = preferenceService.getOrderPreferences(
                order, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Order preferences retrieved successfully", summary));
    }

    @GetMapping("/orders/{orderId}/customer-preferences")
    public ResponseEntity<ApiResponse<ChefViewCustomerPreferenceResponse>> getCustomerPreferences(
            @PathVariable Long orderId,
            @CurrentUser UserPrincipal currentUser) {

        Order order = orderService.findOrderByIdInternal(orderId, currentUser.getRestaurantId());

        // Get first menu item from order to show preferences
        Long menuItemId = order.getOrderItems().isEmpty() ? null :
                order.getOrderItems().get(0).getMenuItemId();

        ChefViewCustomerPreferenceResponse preferences = preferenceService
                .getCustomerPreferencesForOrder(
                        order.getCustomerId(),
                        menuItemId,
                        currentUser
                );

        if (preferences == null) {
            return ResponseEntity.ok(
                    ApiResponse.success("Customer preferences are private", null));
        }

        return ResponseEntity.ok(
                ApiResponse.success("Customer preferences retrieved successfully", preferences));
    }

    @GetMapping("/orders/with-preferences")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOrdersWithPreferences(
            @RequestParam(required = false) Order.OrderStatus status,
            @CurrentUser UserPrincipal currentUser) {

        // Get orders
        List<Order> orders = orderService.getOrdersForKitchen(currentUser, status);

        // Enrich with preferences
        List<Map<String, Object>> enrichedOrders = orders.stream()
                .map(order -> {
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("order", orderService.mapToOrderResponse(order));
                    orderData.put("preferences",
                            preferenceService.getOrderPreferences(order, currentUser));
                    return orderData;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("Orders with preferences retrieved successfully", enrichedOrders));
    }

    @GetMapping("/dietary-alerts")
    public ResponseEntity<ApiResponse<List<OrderPreferencesSummary>>> getDietaryAlerts(
            @CurrentUser UserPrincipal currentUser) {

        // Get active orders with dietary restrictions or allergies
        List<Order> activeOrders = orderService.getActiveOrdersForRestaurant(
                currentUser.getRestaurantId());

        List<OrderPreferencesSummary> alerts = activeOrders.stream()
                .map(order -> preferenceService.getOrderPreferences(order, currentUser))
                .filter(summary ->
                        (summary.getAllergyWarnings() != null && !summary.getAllergyWarnings().isEmpty()) ||
                                (summary.getDietaryRestrictions() != null && !summary.getDietaryRestrictions().isEmpty())
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("Dietary alerts retrieved successfully", alerts));
    }
}

/**
 * Note: Add these methods to OrderService:
 *
 * public Order findOrderByIdInternal(Long orderId, Long restaurantId) {
 *     return orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
 *         .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
 * }
 *
 * public List<Order> getOrdersForKitchen(UserPrincipal currentUser, Order.OrderStatus status) {
 *     if (status != null) {
 *         return orderRepository.findByRestaurantIdAndStatus(
 *             currentUser.getRestaurantId(), status);
 *     }
 *     return orderRepository.findByRestaurantIdAndStatusIn(
 *         currentUser.getRestaurantId(),
 *         Arrays.asList(Order.OrderStatus.CONFIRMED, Order.OrderStatus.PREPARING)
 *     );
 * }
 *
 * public List<Order> getActiveOrdersForRestaurant(Long restaurantId) {
 *     return orderRepository.findByRestaurantIdAndStatusIn(
 *         restaurantId,
 *         Arrays.asList(
 *             Order.OrderStatus.CONFIRMED,
 *             Order.OrderStatus.PREPARING,
 *             Order.OrderStatus.READY
 *         )
 *     );
 * }
 */