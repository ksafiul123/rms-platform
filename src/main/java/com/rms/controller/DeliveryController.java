package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.delivery.*;
import com.rms.security.SecurityUtil;
import com.rms.security.annotation.RequirePermission;
import com.rms.service.delivery.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery Management", description = "Delivery partner assignment and tracking")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/orders/available")
    @RequirePermission("order:read")
    @Operation(summary = "Get available delivery orders",
            description = "Get orders that are ready for delivery assignment")
    public ResponseEntity<ApiResponse<List<DeliveryOrderResponse>>> getAvailableOrders() {
        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        List<DeliveryOrderResponse> orders =
                deliveryService.getAvailableDeliveryOrders(restaurantId);

        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/orders/{orderId}/assign")
    @RequirePermission("order:assign")
    @Operation(summary = "Assign delivery partner",
            description = "Assign a delivery partner to an order")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> assignDeliveryPartner(
            @PathVariable Long orderId,
            @RequestBody AssignDeliveryRequest request) {

        Long assignedBy = SecurityUtil.getCurrentUserId();
        DeliveryAssignmentResponse response =
                deliveryService.assignDeliveryPartner(
                        orderId, request.getDeliveryPartnerId(), assignedBy);

        return ResponseEntity.ok(ApiResponse.success(response, "Delivery partner assigned"));
    }

    @PostMapping("/assignments/{assignmentId}/accept")
    @RequirePermission("order:update")
    @Operation(summary = "Accept delivery",
            description = "Accept a delivery assignment (for delivery partners)")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> acceptDelivery(
            @PathVariable Long assignmentId) {

        Long deliveryPartnerId = SecurityUtil.getCurrentUserId();
        DeliveryAssignmentResponse response =
                deliveryService.acceptDelivery(assignmentId, deliveryPartnerId);

        return ResponseEntity.ok(ApiResponse.success(response, "Delivery accepted"));
    }

    @PostMapping("/assignments/{assignmentId}/pickup")
    @RequirePermission("order:update")
    @Operation(summary = "Mark as picked up",
            description = "Mark order as picked up from restaurant")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> markPickedUp(
            @PathVariable Long assignmentId,
            @RequestBody(required = false) LocationUpdateRequest location) {

        Long deliveryPartnerId = SecurityUtil.getCurrentUserId();
        DeliveryAssignmentResponse response =
                deliveryService.markPickedUp(assignmentId, deliveryPartnerId, location);

        return ResponseEntity.ok(ApiResponse.success(response, "Order picked up"));
    }

    @PatchMapping("/assignments/{assignmentId}/location")
    @RequirePermission("order:update")
    @Operation(summary = "Update delivery location",
            description = "Update current location during delivery (polling endpoint)")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @PathVariable Long assignmentId,
            @RequestBody LocationUpdateRequest location) {

        Long deliveryPartnerId = SecurityUtil.getCurrentUserId();
        deliveryService.updateLocation(assignmentId, deliveryPartnerId, location);

        return ResponseEntity.ok(ApiResponse.success(null, "Location updated"));
    }

    @PostMapping("/assignments/{assignmentId}/deliver")
    @RequirePermission("order:update")
    @Operation(summary = "Mark as delivered",
            description = "Mark order as delivered to customer")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> markDelivered(
            @PathVariable Long assignmentId,
            @RequestBody DeliverOrderRequest request) {

        Long deliveryPartnerId = SecurityUtil.getCurrentUserId();
        DeliveryAssignmentResponse response =
                deliveryService.markDelivered(assignmentId, deliveryPartnerId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Order delivered successfully"));
    }

    @GetMapping("/my-active")
    @RequirePermission("order:read")
    @Operation(summary = "Get my active deliveries",
            description = "Get active delivery assignments for current delivery partner")
    public ResponseEntity<ApiResponse<List<DeliveryAssignmentResponse>>> getMyActiveDeliveries() {
        Long deliveryPartnerId = SecurityUtil.getCurrentUserId();
        List<DeliveryAssignmentResponse> deliveries =
                deliveryService.getActiveDeliveriesForPartner(deliveryPartnerId);

        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }
}
