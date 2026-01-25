package com.rms.controller;

//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.restaurant.*;
import dev.safi.restaurant_management_system.dto.restaurant.*;
import com.rms.service.restaurant.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Restaurant Controller
 * Manages restaurant settings, features, branches, and subscriptions
 */
@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurant Management", description = "Restaurant configuration and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * Get restaurant details
     */
    @GetMapping("/{restaurantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER', 'SALESMAN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @Operation(
            summary = "Get restaurant details",
            description = "Fetch complete restaurant information including settings, subscription, and features"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Restaurant details fetched successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found"
            )
    })
    public ResponseEntity<ApiResponse<RestaurantDetailsResponse>> getRestaurantDetails(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantDetails(restaurantId));
    }

    /**
     * Update restaurant settings
     */
    @PutMapping("/{restaurantId}/settings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @Operation(
            summary = "Update restaurant settings",
            description = "Update restaurant configuration and preferences"
    )
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> updateSettings(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantSettingsRequest request) {
        return ResponseEntity.ok(restaurantService.updateSettings(restaurantId, request));
    }

    /**
     * Toggle single feature
     */
    @PostMapping("/{restaurantId}/features/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Toggle feature",
            description = "Enable or disable a specific feature for the restaurant"
    )
    public ResponseEntity<ApiResponse<FeatureStatusResponse>> toggleFeature(
            @PathVariable Long restaurantId,
            @Valid @RequestBody FeatureToggleRequest request) {
        return ResponseEntity.ok(restaurantService.toggleFeature(restaurantId, request));
    }

    /**
     * Toggle multiple features
     */
    @PostMapping("/{restaurantId}/features/bulk-toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN')")
    @Operation(
            summary = "Bulk toggle features",
            description = "Enable or disable multiple features at once"
    )
    public ResponseEntity<ApiResponse<List<FeatureStatusResponse>>> toggleBulkFeatures(
            @PathVariable Long restaurantId,
            @Valid @RequestBody BulkFeatureToggleRequest request) {
        return ResponseEntity.ok(restaurantService.toggleBulkFeatures(restaurantId, request));
    }

    /**
     * Get all features
     */
    @GetMapping("/{restaurantId}/features")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER', 'SALESMAN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @Operation(
            summary = "Get all features",
            description = "Fetch all available features and their status for the restaurant"
    )
    public ResponseEntity<ApiResponse<List<FeatureStatusResponse>>> getAllFeatures(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getAllFeatures(restaurantId));
    }

    /**
     * Get subscription details
     */
    @GetMapping("/{restaurantId}/subscription")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER', 'SALESMAN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @Operation(
            summary = "Get subscription details",
            description = "Fetch current subscription information"
    )
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getSubscription(restaurantId));
    }

    /**
     * Create branch
     */
    @PostMapping("/{restaurantId}/branches")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Create restaurant branch",
            description = "Add a new branch location (requires MULTI_BRANCH feature enabled)"
    )
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @PathVariable Long restaurantId,
            @Valid @RequestBody BranchCreateRequest request) {
        return ResponseEntity.ok(restaurantService.createBranch(restaurantId, request));
    }

    /**
     * Get all branches
     */
    @GetMapping("/{restaurantId}/branches")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER', 'SALESMAN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @Operation(
            summary = "Get all branches",
            description = "Fetch all branches for the restaurant"
    )
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getAllBranches(restaurantId));
    }
}