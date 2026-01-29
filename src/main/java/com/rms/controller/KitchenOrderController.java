package com.rms.controller;

// KitchenOrderController.java
//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.kitchen.*;
import com.rms.dto.order.OrderDTO;
import com.rms.security.SecurityUtil;
import com.rms.security.annotation.RequirePermission;
import com.rms.service.kitchen.KitchenOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
@Tag(name = "Kitchen Orders", description = "Kitchen order management and preparation tracking")
public class KitchenOrderController {

    private final KitchenOrderService kitchenOrderService;

    @GetMapping("/orders/active")
    @RequirePermission("order:read")
    @Operation(summary = "Get active kitchen orders",
            description = "Get all orders that are currently being prepared in the kitchen")
    public ResponseEntity<ApiResponse<List<KitchenOrderResponse>>> getActiveOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String station) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        List<KitchenOrderResponse> orders =
                kitchenOrderService.getActiveKitchenOrders(restaurantId, status, station);

        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/orders/{orderId}/start")
    @RequirePermission("order:update")
    @Operation(summary = "Start preparing order",
            description = "Mark order as preparing and assign items to chefs")
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> startPreparingOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) StartPreparationRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();
        KitchenOrderResponse response =
                kitchenOrderService.startPreparingOrder(orderId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Order preparation started"));
    }

    @PatchMapping("/orders/{orderId}/items/{itemId}/status")
    @RequirePermission("order:update")
    @Operation(summary = "Update item preparation status",
            description = "Update the preparation status of an individual order item")
    public ResponseEntity<ApiResponse<KitchenOrderItemResponse>> updateItemStatus(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody UpdateItemStatusRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();
        KitchenOrderItemResponse response =
                kitchenOrderService.updateItemStatus(orderId, itemId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/orders/{orderId}/ready")
    @RequirePermission("order:update")
    @Operation(summary = "Mark order as ready",
            description = "Mark order as ready for serving or pickup")
    public ResponseEntity<ApiResponse<OrderDTO.OrderResponse>> markOrderReady(
            @PathVariable Long orderId,
            @RequestBody(required = false) MarkReadyRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();
        OrderDTO.OrderResponse response =
                kitchenOrderService.markOrderReady(orderId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Order is ready"));
    }

    @GetMapping("/metrics")
    @RequirePermission("analytics:view")
    @Operation(summary = "Get kitchen performance metrics",
            description = "Get kitchen performance metrics for a specific date")
    public ResponseEntity<ApiResponse<KitchenMetricsResponse>> getKitchenMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        KitchenMetricsResponse metrics =
                kitchenOrderService.getKitchenMetrics(restaurantId, date);

        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}