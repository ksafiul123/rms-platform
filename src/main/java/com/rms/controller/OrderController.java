package com.rms.controller;

//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.order.OrderDTO.*;
import com.rms.entity.Order;
import com.rms.security.CurrentUser;
import com.rms.security.UserPrincipal;
import com.rms.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @CurrentUser UserPrincipal currentUser) {

        OrderResponse order = orderService.createOrder(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) Order.OrderType orderType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<OrderSummaryResponse> orders = orderService.getOrders(
                currentUser, status, orderType, pageable);

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long orderId,
            @CurrentUser UserPrincipal currentUser) {

        OrderResponse order = orderService.getOrderById(orderId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CHEF', 'DELIVERY_MAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @CurrentUser UserPrincipal currentUser) {

        OrderResponse order = orderService.updateOrderStatus(orderId, request, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request,
            @CurrentUser UserPrincipal currentUser) {

        OrderResponse order = orderService.cancelOrder(orderId, request, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    @PutMapping("/{orderId}/assign-delivery")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> assignDeliveryMan(
            @PathVariable Long orderId,
            @Valid @RequestBody AssignDeliveryManRequest request,
            @CurrentUser UserPrincipal currentUser) {

        OrderResponse order = orderService.assignDeliveryMan(orderId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Delivery man assigned successfully", order));
    }

    @GetMapping("/{orderId}/history")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getOrderHistory(
            @PathVariable Long orderId,
            @CurrentUser UserPrincipal currentUser) {

        List<OrderStatusHistoryResponse> history = orderService.getOrderHistory(
                orderId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Order history retrieved successfully", history));
    }

    // Customer-specific endpoints
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<OrderSummaryResponse> orders = orderService.getOrders(
                currentUser, null, null, pageable);

        return ResponseEntity.ok(ApiResponse.success("Your orders retrieved successfully", orders));
    }

    // Chef-specific endpoints
    @GetMapping("/kitchen/pending")
    @PreAuthorize("hasAnyRole('CHEF', 'RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getPendingKitchenOrders(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<OrderSummaryResponse> orders = orderService.getOrders(
                currentUser, Order.OrderStatus.CONFIRMED, null, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Pending kitchen orders retrieved successfully", orders));
    }

    // Delivery man-specific endpoints
    @GetMapping("/delivery/my-deliveries")
    @PreAuthorize("hasRole('DELIVERY_MAN')")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getMyDeliveries(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<OrderSummaryResponse> orders = orderService.getOrders(
                currentUser, null, Order.OrderType.DELIVERY, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Your deliveries retrieved successfully", orders));
    }
}
