package com.rms.controller;

//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.InventoryDTO.*;
import com.rms.entity.InventoryItem;
import com.rms.entity.LowStockAlert;
import com.rms.security.CurrentUser;
import com.rms.security.UserPrincipal;
import com.rms.service.InventoryAlertService;
import com.rms.service.InventoryService;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryAlertService alertService;

    // ========== Inventory Item Management ==========

    @PostMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> createInventoryItem(
            @Valid @RequestBody CreateInventoryItemRequest request,
            @CurrentUser UserPrincipal currentUser) {

        InventoryItemResponse item = inventoryService.createInventoryItem(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory item created successfully", item));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<InventoryItemResponse>>> getInventoryItems(
            @RequestParam(required = false) InventoryItem.InventoryCategory category,
            @RequestParam(required = false) InventoryItem.InventoryStatus status,
            @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<InventoryItemResponse> items = inventoryService.getInventoryItems(
                currentUser, category, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inventory items retrieved successfully", items));
    }

    @GetMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> getInventoryItem(
            @PathVariable Long itemId,
            @CurrentUser UserPrincipal currentUser) {

        InventoryItemResponse item = inventoryService.getInventoryItem(itemId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Inventory item retrieved successfully", item));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateInventoryItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateInventoryItemRequest request,
            @CurrentUser UserPrincipal currentUser) {

        InventoryItemResponse item = inventoryService.updateInventoryItem(
                itemId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Inventory item updated successfully", item));
    }

    // ========== Stock Management ==========

    @PostMapping("/{itemId}/add-stock")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> addStock(
            @PathVariable Long itemId,
            @Valid @RequestBody AddStockRequest request,
            @CurrentUser UserPrincipal currentUser) {

        inventoryService.addStock(itemId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Stock added successfully", null));
    }

    @PostMapping("/{itemId}/deduct-stock")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deductStock(
            @PathVariable Long itemId,
            @Valid @RequestBody DeductStockRequest request,
            @CurrentUser UserPrincipal currentUser) {

        inventoryService.deductStock(itemId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Stock deducted successfully", null));
    }

    @GetMapping("/{itemId}/transactions")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<StockTransactionResponse>>> getStockTransactions(
            @PathVariable Long itemId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<StockTransactionResponse> transactions = inventoryService.getStockTransactions(
                itemId, currentUser, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Stock transactions retrieved successfully", transactions));
    }

    // ========== Menu Item Linking ==========

    @PostMapping("/{itemId}/link-menu-item")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemInventoryResponse>> linkInventoryToMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody LinkMenuItemRequest request,
            @CurrentUser UserPrincipal currentUser) {

        MenuItemInventoryResponse link = inventoryService.linkInventoryToMenuItem(
                itemId, request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory item linked to menu item successfully", link));
    }

    @GetMapping("/menu-item/{menuItemId}/availability")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<StockAvailabilityResponse>> checkMenuItemAvailability(
            @PathVariable Long menuItemId,
            @RequestParam(defaultValue = "1") int quantity) {

        StockAvailabilityResponse availability = inventoryService.checkMenuItemAvailability(
                menuItemId, quantity);

        return ResponseEntity.ok(
                ApiResponse.success("Availability checked successfully", availability));
    }

    // ========== Alerts & Reports ==========

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getLowStockItems(
            @CurrentUser UserPrincipal currentUser) {

        List<InventoryItemResponse> items = inventoryService.getLowStockItems(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Low stock items retrieved successfully", items));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<LowStockAlertResponse>>> getAlerts(
            @RequestParam(required = false) LowStockAlert.AlertStatus status,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<LowStockAlertResponse> alerts = alertService.getAlerts(
                currentUser, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    @GetMapping("/alerts/active")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<LowStockAlertResponse>>> getActiveAlerts(
            @CurrentUser UserPrincipal currentUser) {

        List<LowStockAlertResponse> alerts = alertService.getActiveAlerts(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Active alerts retrieved successfully", alerts));
    }

    @GetMapping("/alerts/count")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getActiveAlertsCount(
            @CurrentUser UserPrincipal currentUser) {

        Long count = alertService.getActiveAlertsCount(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Alert count retrieved successfully", count));
    }

    @PostMapping("/alerts/{alertId}/acknowledge")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<LowStockAlertResponse>> acknowledgeAlert(
            @PathVariable Long alertId,
            @CurrentUser UserPrincipal currentUser) {

        LowStockAlertResponse alert = alertService.acknowledgeAlert(alertId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Alert acknowledged successfully", alert));
    }

    @PostMapping("/alerts/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resolveAlert(
            @PathVariable Long alertId,
            @CurrentUser UserPrincipal currentUser) {

        alertService.resolveAlert(alertId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Alert resolved successfully", null));
    }
}
