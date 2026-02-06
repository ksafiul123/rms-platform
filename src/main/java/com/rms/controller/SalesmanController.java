package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.restaurant.OnboardingStatusResponse;
import com.rms.dto.restaurant.RestaurantListResponse;
import com.rms.dto.restaurant.SalesmanPerformanceResponse;
import com.rms.dto.restaurant.SubscriptionPlanResponse;
import com.rms.dto.restaurant.SubscriptionResponse;
import com.rms.enums.OnboardingStatus;
import com.rms.service.restaurant.SalesmanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Salesman Controller
 * Manages salesman operations and restaurant onboarding
 */
@RestController
@RequestMapping("/api/v1/salesman")
@RequiredArgsConstructor
@Tag(name = "Salesman Management", description = "Salesman and onboarding management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SalesmanController {

    private final SalesmanService salesmanService;

    /**
     * Register salesman
     */
    @PostMapping("/register/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Register salesman",
            description = "Register an existing user as a salesman (Super Admin only)"
    )
    public ResponseEntity<ApiResponse<SalesmanPerformanceResponse>> registerSalesman(
            @PathVariable Long userId) {
        return ResponseEntity.ok(salesmanService.registerSalesman(userId));
    }

    /**
     * Initiate restaurant onboarding
     */
    @PostMapping("/onboard/{restaurantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN')")
    @Operation(
            summary = "Initiate restaurant onboarding",
            description = "Start the onboarding process for a newly registered restaurant"
    )
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> initiateOnboarding(
            @PathVariable Long restaurantId,
            @RequestParam Long salesmanId) {
        return ResponseEntity.ok(salesmanService.initiateOnboarding(restaurantId, salesmanId));
    }

    /**
     * Update onboarding status
     */
    @PutMapping("/onboarding/{restaurantId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN')")
    @Operation(
            summary = "Update onboarding status",
            description = "Update the onboarding progress and status"
    )
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> updateOnboardingStatus(
            @PathVariable Long restaurantId,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(
                salesmanService.updateOnboardingStatus(restaurantId, OnboardingStatus.valueOf(status), notes)
        );
    }

    /**
     * Get onboarding status
     */
    @GetMapping("/onboarding/{restaurantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Get onboarding status",
            description = "Fetch current onboarding progress"
    )
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getOnboardingStatus(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(salesmanService.getOnboardingStatus(restaurantId));
    }

    /**
     * Get salesman performance
     */
    @GetMapping("/{salesmanId}/performance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN')")
    @Operation(
            summary = "Get salesman performance",
            description = "Fetch performance metrics for a salesman"
    )
    public ResponseEntity<ApiResponse<SalesmanPerformanceResponse>> getSalesmanPerformance(
            @PathVariable Long salesmanId) {
        return ResponseEntity.ok(salesmanService.getSalesmanPerformance(salesmanId));
    }

    /**
     * Get salesman's restaurants
     */
    @GetMapping("/{salesmanId}/restaurants")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN')")
    @Operation(
            summary = "Get salesman's restaurants",
            description = "Fetch all restaurants onboarded by a specific salesman"
    )
    public ResponseEntity<ApiResponse<List<RestaurantListResponse>>> getSalesmanRestaurants(
            @PathVariable Long salesmanId) {
        return ResponseEntity.ok(salesmanService.getSalesmanRestaurants(salesmanId));
    }

    /**
     * Assign subscription plan
     */
    @PostMapping("/subscription/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN')")
    @Operation(
            summary = "Assign subscription plan",
            description = "Assign a subscription plan to a restaurant"
    )
    public ResponseEntity<ApiResponse<SubscriptionResponse>> assignSubscriptionPlan(
            @RequestParam Long restaurantId,
            @RequestParam String planName,
            @RequestParam String billingCycle) {
        return ResponseEntity.ok(
                salesmanService.assignSubscriptionPlan(restaurantId, planName, billingCycle)
        );
    }

    /**
     * Get all subscription plans
     */
    @GetMapping("/subscription/plans")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALESMAN', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Get subscription plans",
            description = "Fetch all available subscription plans"
    )
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllPlans() {
        return ResponseEntity.ok(salesmanService.getAllPlans());
    }
}
