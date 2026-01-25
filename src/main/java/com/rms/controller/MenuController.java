package com.rms.controller;

//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.menu.*;
import dev.safi.restaurant_management_system.dto.menu.*;
import com.rms.security.annotation.RequirePermission;
import com.rms.service.menu.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Menu Controller
 * Manages menu categories, items, pricing, and modifiers
 */
@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Tag(name = "Menu Management", description = "Menu categories, items, and modifiers management")
@SecurityRequirement(name = "bearerAuth")
public class MenuController {

    private final MenuService menuService;

    // ==================== CATEGORY ENDPOINTS ====================

    /**
     * Create menu category
     */
    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @RequirePermission(value = "menu:create", resource = "category")
    @Operation(
            summary = "Create menu category",
            description = "Create a new menu category (supports nested categories)"
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestParam Long restaurantId,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.createCategory(restaurantId, request));
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    @RequirePermission(value = "menu:read", resource = "category")
    @Operation(
            summary = "Get all categories",
            description = "Fetch all menu categories for a restaurant"
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @RequestParam Long restaurantId) {
        return ResponseEntity.ok(menuService.getAllCategories(restaurantId));
    }

    /**
     * Get root categories (top-level only with children)
     */
    @GetMapping("/categories/root")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get root categories",
            description = "Fetch top-level categories with their subcategories"
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories(
            @RequestParam Long restaurantId) {
        return ResponseEntity.ok(menuService.getRootCategories(restaurantId));
    }

    /**
     * Update category
     */
    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @RequirePermission(value = "menu:update", resource = "category")
    @Operation(
            summary = "Update menu category",
            description = "Update an existing menu category"
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @RequestParam Long restaurantId,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(menuService.updateCategory(restaurantId, categoryId, request));
    }

    // ==================== MENU ITEM ENDPOINTS ====================

    /**
     * Create menu item
     */
    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @RequirePermission(value = "menu:create", resource = "menu_item")
    @Operation(
            summary = "Create menu item",
            description = "Create a new menu item with variants, modifiers, and ingredients"
    )
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @RequestParam Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.createMenuItem(restaurantId, request));
    }

    /**
     * Get menu items by category
     */
    @GetMapping("/items/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @RequirePermission(value = "menu:read", resource = "menu_item")
    @Operation(
            summary = "Get items by category",
            description = "Fetch all menu items in a specific category"
    )
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getItemsByCategory(
            @RequestParam Long restaurantId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.getItemsByCategory(restaurantId, categoryId));
    }

    /**
     * Get menu item by ID
     */
    @GetMapping("/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @RequirePermission(value = "menu:read", resource = "menu_item")
    @Operation(
            summary = "Get menu item",
            description = "Fetch detailed information about a menu item"
    )
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItem(
            @RequestParam Long restaurantId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(menuService.getMenuItem(restaurantId, itemId));
    }

    /**
     * Update menu item
     */
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @RequirePermission(value = "menu:update", resource = "menu_item")
    @Operation(
            summary = "Update menu item",
            description = "Update an existing menu item"
    )
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @RequestParam Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.updateMenuItem(restaurantId, itemId, request));
    }

    /**
     * Toggle item availability
     */
    @PatchMapping("/items/{itemId}/availability")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER', 'CHEF')")
    @RequirePermission(value = "menu:update", resource = "menu_item")
    @Operation(
            summary = "Toggle item availability",
            description = "Mark item as available or unavailable"
    )
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @RequestParam Long restaurantId,
            @PathVariable Long itemId,
            @RequestParam Boolean isAvailable) {
        return ResponseEntity.ok(menuService.toggleAvailability(restaurantId, itemId, isAvailable));
    }

    /**
     * Bulk update availability
     */
    @PatchMapping("/items/bulk-availability")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @RequirePermission(value = "menu:manage", resource = "menu_item")
    @Operation(
            summary = "Bulk update availability",
            description = "Update availability for multiple items at once"
    )
    public ResponseEntity<ApiResponse<Void>> bulkUpdateAvailability(
            @RequestParam Long restaurantId,
            @Valid @RequestBody BulkAvailabilityRequest request) {
        return ResponseEntity.ok(menuService.bulkUpdateAvailability(restaurantId, request));
    }

    /**
     * Get featured items
     */
    @GetMapping("/items/featured")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get featured items",
            description = "Fetch all featured menu items"
    )
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getFeaturedItems(
            @RequestParam Long restaurantId) {
        return ResponseEntity.ok(menuService.getFeaturedItems(restaurantId));
    }

    /**
     * Get menu statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @RequirePermission(value = "analytics:view", resource = "menu")
    @Operation(
            summary = "Get menu statistics",
            description = "Fetch comprehensive menu statistics"
    )
    public ResponseEntity<ApiResponse<MenuStatsResponse>> getMenuStats(
            @RequestParam Long restaurantId) {
        return ResponseEntity.ok(menuService.getMenuStats(restaurantId));
    }

    // ==================== MODIFIER ENDPOINTS ====================

    /**
     * Create modifier group
     */
    @PostMapping("/modifiers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @RequirePermission(value = "menu:create", resource = "modifier")
    @Operation(
            summary = "Create modifier group",
            description = "Create a new modifier group with options (e.g., spice levels, add-ons)"
    )
    public ResponseEntity<ApiResponse<ModifierGroupResponse>> createModifierGroup(
            @RequestParam Long restaurantId,
            @Valid @RequestBody ModifierGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.createModifierGroup(restaurantId, request));
    }
}

