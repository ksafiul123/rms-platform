package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.tracking.DeliveryTrackingResponse;
import com.rms.dto.tracking.EstimatedTimeResponse;
import com.rms.dto.tracking.LiveOrderStatusResponse;
import com.rms.dto.tracking.OrderTimelineResponse;
import com.rms.security.SecurityUtil;
import com.rms.service.tracking.OrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Tracking", description = "Customer order tracking and live status updates")
public class OrderTrackingController {

    private final OrderTrackingService orderTrackingService;

    @GetMapping("/{orderId}/live-status")
    @Operation(summary = "Get live order status",
            description = "Get real-time order status with progress tracking")
    public ResponseEntity<ApiResponse<LiveOrderStatusResponse>> getLiveOrderStatus(
            @PathVariable Long orderId) {

        Long customerId = SecurityUtil.getCurrentUserId();
        LiveOrderStatusResponse status =
                orderTrackingService.getLiveOrderStatus(orderId, customerId);

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/{orderId}/timeline")
    @Operation(summary = "Get order timeline",
            description = "Get complete timeline of order events")
    public ResponseEntity<ApiResponse<List<OrderTimelineResponse>>> getOrderTimeline(
            @PathVariable Long orderId) {

        Long customerId = SecurityUtil.getCurrentUserId();
        List<OrderTimelineResponse> timeline =
                orderTrackingService.getOrderTimeline(orderId, customerId);

        return ResponseEntity.ok(ApiResponse.success(timeline));
    }

    @GetMapping("/{orderId}/delivery-tracking")
    @Operation(summary = "Get delivery tracking",
            description = "Get real-time delivery location and tracking")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> getDeliveryTracking(
            @PathVariable Long orderId) {

        Long customerId = SecurityUtil.getCurrentUserId();
        DeliveryTrackingResponse tracking =
                orderTrackingService.getDeliveryTracking(orderId, customerId);

        return ResponseEntity.ok(ApiResponse.success(tracking));
    }

    @GetMapping("/{orderId}/estimated-time")
    @Operation(summary = "Get estimated time",
            description = "Get estimated time for order completion")
    public ResponseEntity<ApiResponse<EstimatedTimeResponse>> getEstimatedTime(
            @PathVariable Long orderId) {

        EstimatedTimeResponse time =
                orderTrackingService.getEstimatedTime(orderId);

        return ResponseEntity.ok(ApiResponse.success(time));
    }
}
