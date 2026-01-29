package com.rms.service;

//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.PreferenceDTO.*;
import com.rms.security.CurrentUser;
import com.rms.security.UserPrincipal;
import com.rms.service.CustomerPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final CustomerPreferenceService preferenceService;

    // ========== Customer Preferences ==========

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerPreferenceResponse>> getMyPreferences(
            @CurrentUser UserPrincipal currentUser) {

        CustomerPreferenceResponse preferences = preferenceService.getMyPreferences(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Preferences retrieved successfully", preferences));
    }

    @PutMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerPreferenceResponse>> updateMyPreferences(
            @Valid @RequestBody UpdateCustomerPreferenceRequest request,
            @CurrentUser UserPrincipal currentUser) {

        CustomerPreferenceResponse preferences = preferenceService.updatePreferences(
                request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Preferences updated successfully", preferences));
    }

    @GetMapping("/my/summary")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PreferenceSummaryResponse>> getPreferenceSummary(
            @CurrentUser UserPrincipal currentUser) {

        PreferenceSummaryResponse summary = preferenceService.getPreferenceSummary(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Preference summary retrieved successfully", summary));
    }

    // ========== Favorite Menu Items ==========

    @PostMapping("/favorites")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<FavoriteMenuItemResponse>> addFavorite(
            @Valid @RequestBody AddFavoriteRequest request,
            @CurrentUser UserPrincipal currentUser) {

        FavoriteMenuItemResponse favorite = preferenceService.addFavorite(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Favorite added successfully", favorite));
    }

    @GetMapping("/favorites")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<FavoriteMenuItemResponse>>> getMyFavorites(
            @CurrentUser UserPrincipal currentUser) {

        List<FavoriteMenuItemResponse> favorites = preferenceService.getMyFavorites(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Favorites retrieved successfully", favorites));
    }

    @GetMapping("/favorites/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<FavoriteMenuItemResponse>>> getFavoritesByRestaurant(
            @PathVariable Long restaurantId,
            @CurrentUser UserPrincipal currentUser) {

        List<FavoriteMenuItemResponse> favorites = preferenceService
                .getMyFavoritesByRestaurant(restaurantId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Restaurant favorites retrieved successfully", favorites));
    }

    @DeleteMapping("/favorites/{menuItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long menuItemId,
            @CurrentUser UserPrincipal currentUser) {

        preferenceService.removeFavorite(menuItemId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Favorite removed successfully", null));
    }

    // ========== Menu Item Specific Preferences ==========

    @PutMapping("/menu-items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<MenuItemPreferenceResponse>> updateMenuItemPreference(
            @Valid @RequestBody UpdateMenuItemPreferenceRequest request,
            @CurrentUser UserPrincipal currentUser) {

        MenuItemPreferenceResponse preference = preferenceService
                .updateMenuItemPreference(request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Menu item preference updated successfully", preference));
    }

    @GetMapping("/menu-items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<MenuItemPreferenceResponse>>> getMyMenuItemPreferences(
            @CurrentUser UserPrincipal currentUser) {

        List<MenuItemPreferenceResponse> preferences = preferenceService
                .getMyMenuItemPreferences(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Menu item preferences retrieved successfully", preferences));
    }

    @DeleteMapping("/menu-items/{menuItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItemPreference(
            @PathVariable Long menuItemId,
            @CurrentUser UserPrincipal currentUser) {

        preferenceService.deleteMenuItemPreference(menuItemId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Menu item preference deleted successfully", null));
    }

    // ========== Chef Access to Preferences ==========

    @GetMapping("/customer/{customerId}/menu-item/{menuItemId}")
    @PreAuthorize("hasAnyRole('CHEF', 'RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChefViewCustomerPreferenceResponse>> getCustomerPreferences(
            @PathVariable Long customerId,
            @PathVariable Long menuItemId,
            @CurrentUser UserPrincipal currentUser) {

        ChefViewCustomerPreferenceResponse preferences = preferenceService
                .getCustomerPreferencesForOrder(customerId, menuItemId, currentUser);

        if (preferences == null) {
            return ResponseEntity.ok(
                    ApiResponse.success("Customer preferences are private", null));
        }

        return ResponseEntity.ok(
                ApiResponse.success("Customer preferences retrieved successfully", preferences));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('CHEF', 'RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderPreferencesSummary>> getOrderPreferences(
            @PathVariable Long orderId,
            @CurrentUser UserPrincipal currentUser) {

        // This would require OrderRepository injection
        // OrderPreferencesSummary summary = preferenceService
        //     .getOrderPreferences(order, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Order preferences retrieved successfully", null));
    }
}
