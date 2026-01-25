package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.menu.CategoryResponse;
import com.rms.dto.menu.MenuItemResponse;
import com.rms.service.menu.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public Menu Controller - For customer-facing menu
 */
@RestController
@RequestMapping("/api/v1/public/menu")
@RequiredArgsConstructor
@Tag(name = "Public Menu", description = "Customer-facing menu endpoints (no auth required)")
public class PublicMenuController {

    private final MenuService menuService;

    /**
     * Get restaurant menu (public)
     */
    @GetMapping("/{restaurantId}/categories")
    @Operation(
            summary = "Get public menu categories",
            description = "Fetch menu categories for public display"
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getPublicCategories(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getRootCategories(restaurantId));
    }

    /**
     * Get items by category (public)
     */
    @GetMapping("/{restaurantId}/category/{categoryId}/items")
    @Operation(
            summary = "Get public menu items",
            description = "Fetch menu items in a category for public display"
    )
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getPublicItems(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.getItemsByCategory(restaurantId, categoryId));
    }

    /**
     * Get item details (public)
     */
    @GetMapping("/{restaurantId}/items/{itemId}")
    @Operation(
            summary = "Get public item details",
            description = "Fetch detailed menu item information for public display"
    )
    public ResponseEntity<ApiResponse<MenuItemResponse>> getPublicItemDetails(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(menuService.getMenuItem(restaurantId, itemId));
    }

    /**
     * Get featured items (public)
     */
    @GetMapping("/{restaurantId}/featured")
    @Operation(
            summary = "Get public featured items",
            description = "Fetch featured menu items for public display"
    )
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getPublicFeatured(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getFeaturedItems(restaurantId));
    }
}
