package com.rms.service.restaurant;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.restaurant.BranchCreateRequest;
import com.rms.dto.restaurant.BranchResponse;
import com.rms.dto.restaurant.BulkFeatureToggleRequest;
import com.rms.dto.restaurant.FeatureStatusResponse;
import com.rms.dto.restaurant.FeatureToggleRequest;
import com.rms.dto.restaurant.OnboardingStatusResponse;
import com.rms.dto.restaurant.RestaurantDetailsResponse;
import com.rms.dto.restaurant.RestaurantListResponse;
import com.rms.dto.restaurant.RestaurantSettingsRequest;
import com.rms.dto.restaurant.RestaurantSettingsResponse;
import com.rms.dto.restaurant.SubscriptionPlanResponse;
import com.rms.dto.restaurant.SubscriptionResponse;
import com.rms.entity.Restaurant;
import com.rms.entity.RestaurantBranch;
import com.rms.entity.RestaurantFeature;
import com.rms.entity.RestaurantSettings;
import com.rms.entity.RestaurantSubscription;
import com.rms.entity.SubscriptionPlan;
import com.rms.entity.User;
import com.rms.enums.BusinessType;
import com.rms.enums.FeatureName;
import com.rms.enums.SubscriptionStatus;
import com.rms.exception.BadRequestException;
import com.rms.exception.InsufficientPermissionException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.RestaurantBranchRepository;
import com.rms.repository.RestaurantFeatureRepository;
import com.rms.repository.RestaurantRepository;
import com.rms.repository.RestaurantSettingsRepository;
import com.rms.repository.RestaurantSubscriptionRepository;
import com.rms.repository.SubscriptionPlanRepository;
import com.rms.repository.UserRepository;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Restaurant Service - Manages restaurant operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantSettingsRepository settingsRepository;
    private final RestaurantFeatureRepository featureRepository;
    private final RestaurantSubscriptionRepository subscriptionRepository;
    private final RestaurantBranchRepository branchRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;

    /**
     * Get restaurant details with settings, subscription, and features
     */
    @Transactional(readOnly = true)
    public ApiResponse<RestaurantDetailsResponse> getRestaurantDetails(Long restaurantId) {
        validateRestaurantAccess(restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        RestaurantDetailsResponse response = buildRestaurantDetailsResponse(restaurant);

        return ApiResponse.success("Restaurant details fetched successfully", response);
    }

    /**
     * Update restaurant settings
     */
    @Transactional
    public ApiResponse<RestaurantSettingsResponse> updateSettings(
            Long restaurantId,
            RestaurantSettingsRequest request) {

        validateRestaurantAccess(restaurantId);

        RestaurantSettings settings = settingsRepository.findByRestaurantId(restaurantId)
                .orElse(RestaurantSettings.builder()
                        .restaurantId(restaurantId)
                        .build());

        // Update settings
        if (request.getBusinessName() != null) {
            settings.setBusinessName(request.getBusinessName());
        }
        if (request.getBusinessType() != null) {
            settings.setBusinessType(BusinessType.valueOf(request.getBusinessType()));
        }
        if (request.getCurrency() != null) {
            settings.setCurrency(request.getCurrency());
        }
        if (request.getTimezone() != null) {
            settings.setTimezone(request.getTimezone());
        }
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        if (request.getTaxPercentage() != null) {
            settings.setTaxPercentage(request.getTaxPercentage());
        }
        if (request.getServiceChargePercentage() != null) {
            settings.setServiceChargePercentage(request.getServiceChargePercentage());
        }
        if (request.getAutoAcceptOrders() != null) {
            settings.setAutoAcceptOrders(request.getAutoAcceptOrders());
        }
        if (request.getAllowOnlinePayments() != null) {
            settings.setAllowOnlinePayments(request.getAllowOnlinePayments());
        }
        if (request.getAllowCashPayments() != null) {
            settings.setAllowCashPayments(request.getAllowCashPayments());
        }
        if (request.getMinimumOrderAmount() != null) {
            settings.setMinimumOrderAmount(request.getMinimumOrderAmount());
        }
        if (request.getDeliveryRadiusKm() != null) {
            settings.setDeliveryRadiusKm(request.getDeliveryRadiusKm());
        }
        if (request.getAveragePreparationTimeMinutes() != null) {
            settings.setAveragePreparationTimeMinutes(request.getAveragePreparationTimeMinutes());
        }
        if (request.getLogoUrl() != null) {
            settings.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBannerUrl() != null) {
            settings.setBannerUrl(request.getBannerUrl());
        }

        settings = settingsRepository.save(settings);

        log.info("Restaurant settings updated for restaurant ID: {}", restaurantId);

        return ApiResponse.success("Settings updated successfully", mapToSettingsResponse(settings));
    }

    /**
     * Toggle single feature
     */
    @Transactional
    public ApiResponse<FeatureStatusResponse> toggleFeature(
            Long restaurantId,
            FeatureToggleRequest request) {

        validateRestaurantAccess(restaurantId);

        FeatureName featureName;
        try {
            featureName = FeatureName.valueOf(request.getFeatureName());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid feature name: " + request.getFeatureName());
        }

        // Check if feature exists
        RestaurantFeature feature = featureRepository
                .findByRestaurantIdAndFeatureName(restaurantId, featureName)
                .orElse(RestaurantFeature.builder()
                        .restaurantId(restaurantId)
                        .featureName(featureName)
                        .isEnabled(false)
                        .build());

        // Validate subscription allows this feature
        validateFeatureAccess(restaurantId, featureName, request.getIsEnabled());

        // Update feature
        feature.setIsEnabled(request.getIsEnabled());
        feature.setEnabledAt(request.getIsEnabled() ? LocalDateTime.now() : null);
        feature.setEnabledBy(getCurrentUserId());
        feature.setNotes(request.getNotes());

        feature = featureRepository.save(feature);

        log.info("Feature {} {} for restaurant ID: {}",
                featureName,
                request.getIsEnabled() ? "enabled" : "disabled",
                restaurantId);

        return ApiResponse.success(
                "Feature " + (request.getIsEnabled() ? "enabled" : "disabled") + " successfully",
                mapToFeatureStatusResponse(feature)
        );
    }

    /**
     * Toggle multiple features at once
     */
    @Transactional
    public ApiResponse<List<FeatureStatusResponse>> toggleBulkFeatures(
            Long restaurantId,
            BulkFeatureToggleRequest request) {

        validateRestaurantAccess(restaurantId);

        List<FeatureStatusResponse> responses = new ArrayList<>();
        Long currentUserId = getCurrentUserId();

        for (Map.Entry<String, Boolean> entry : request.getFeatures().entrySet()) {
            try {
                FeatureName featureName = FeatureName.valueOf(entry.getKey());

                RestaurantFeature feature = featureRepository
                        .findByRestaurantIdAndFeatureName(restaurantId, featureName)
                        .orElse(RestaurantFeature.builder()
                                .restaurantId(restaurantId)
                                .featureName(featureName)
                                .isEnabled(false)
                                .build());

                feature.setIsEnabled(entry.getValue());
                feature.setEnabledAt(entry.getValue() ? LocalDateTime.now() : null);
                feature.setEnabledBy(currentUserId);
                feature.setNotes(request.getNotes());

                feature = featureRepository.save(feature);
                responses.add(mapToFeatureStatusResponse(feature));

            } catch (IllegalArgumentException e) {
                log.warn("Invalid feature name: {}", entry.getKey());
            }
        }

        log.info("Bulk feature update completed for restaurant ID: {}", restaurantId);

        return ApiResponse.success("Features updated successfully", responses);
    }

    /**
     * Get all features for a restaurant
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<FeatureStatusResponse>> getAllFeatures(Long restaurantId) {
        validateRestaurantAccess(restaurantId);

        List<RestaurantFeature> features = featureRepository.findByRestaurantId(restaurantId);

        // If no features exist, create default ones
        if (features.isEmpty()) {
            features = initializeDefaultFeatures(restaurantId);
        }

        List<FeatureStatusResponse> responses = features.stream()
                .map(this::mapToFeatureStatusResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Features fetched successfully", responses);
    }

    /**
     * Check if specific feature is enabled
     */
    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(Long restaurantId, String featureName) {
        try {
            FeatureName feature = FeatureName.valueOf(featureName);
            return featureRepository.existsByRestaurantIdAndFeatureNameAndIsEnabledTrue(
                    restaurantId, feature);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get subscription details
     */
    @Transactional(readOnly = true)
    public ApiResponse<SubscriptionResponse> getSubscription(Long restaurantId) {
        validateRestaurantAccess(restaurantId);

        RestaurantSubscription subscription = subscriptionRepository
                .findLatestByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("No subscription found"));

        SubscriptionResponse response = mapToSubscriptionResponse(subscription);

        return ApiResponse.success("Subscription details fetched successfully", response);
    }

    /**
     * Create or update branch
     */
    @Transactional
    public ApiResponse<BranchResponse> createBranch(Long restaurantId, BranchCreateRequest request) {
        validateRestaurantAccess(restaurantId);

        // Check if multi-branch feature is enabled
        if (!isFeatureEnabled(restaurantId, "MULTI_BRANCH")) {
            throw new BadRequestException("Multi-branch feature is not enabled for this restaurant");
        }

        // Generate unique branch code
        String branchCode = generateBranchCode(restaurantId);

        RestaurantBranch branch = RestaurantBranch.builder()
                .restaurantId(restaurantId)
                .branchCode(branchCode)
                .branchName(request.getBranchName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .zipCode(request.getZipCode())
                .state(request.getState())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .isMainBranch(request.getIsMainBranch())
                .isActive(true)
                .build();

        branch = branchRepository.save(branch);

        log.info("Branch created: {} for restaurant ID: {}", branchCode, restaurantId);

        return ApiResponse.success("Branch created successfully", mapToBranchResponse(branch));
    }

    /**
     * Get all branches for restaurant
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<BranchResponse>> getAllBranches(Long restaurantId) {
        validateRestaurantAccess(restaurantId);

        List<RestaurantBranch> branches = branchRepository.findByRestaurantId(restaurantId);

        List<BranchResponse> responses = branches.stream()
                .map(this::mapToBranchResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Branches fetched successfully", responses);
    }

    // ==================== Helper Methods ====================

    private void validateRestaurantAccess(Long restaurantId) {
        UserPrincipal principal = getCurrentUser();

        // Super Admin and Developer can access all restaurants
        if (hasRole(principal, "ROLE_SUPER_ADMIN") || hasRole(principal, "ROLE_DEVELOPER")) {
            return;
        }

        // Salesman can access restaurants they onboarded
        if (hasRole(principal, "ROLE_SALESMAN")) {
            // TODO: Check if salesman onboarded this restaurant
            return;
        }

        // Restaurant staff can only access their own restaurant
        if (principal.getRestaurantId() == null || !principal.getRestaurantId().equals(restaurantId)) {
            throw new InsufficientPermissionException("Access denied to this restaurant");
        }
    }

    private void validateFeatureAccess(Long restaurantId, FeatureName featureName, Boolean enable) {
        if (!enable) {
            return; // Can always disable features
        }

        // Check subscription limits
        RestaurantSubscription subscription = subscriptionRepository
                .findLatestByRestaurantId(restaurantId)
                .orElseThrow(() -> new BadRequestException("No active subscription found"));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE &&
                subscription.getStatus() != SubscriptionStatus.TRIAL) {
            throw new BadRequestException("Cannot enable features without active subscription");
        }

        // Premium features validation can be added here
    }

    private List<RestaurantFeature> initializeDefaultFeatures(Long restaurantId) {
        List<RestaurantFeature> features = new ArrayList<>();

        // Create all features as disabled by default
        for (FeatureName featureName : FeatureName.values()) {
            RestaurantFeature feature = RestaurantFeature.builder()
                    .restaurantId(restaurantId)
                    .featureName(featureName)
                    .isEnabled(false)
                    .build();
            features.add(feature);
        }

        return featureRepository.saveAll(features);
    }

    private String generateBranchCode(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Long branchCount = branchRepository.countActiveByRestaurantId(restaurantId);

        return restaurant.getRestaurantCode() + "-B" + String.format("%03d", branchCount + 1);
    }

    private RestaurantDetailsResponse buildRestaurantDetailsResponse(Restaurant restaurant) {
        RestaurantSettingsResponse settings = settingsRepository.findByRestaurantId(restaurant.getId())
                .map(this::mapToSettingsResponse)
                .orElse(null);

        SubscriptionResponse subscription = subscriptionRepository.findLatestByRestaurantId(restaurant.getId())
                .map(this::mapToSubscriptionResponse)
                .orElse(null);

        List<String> enabledFeatures = featureRepository.findEnabledFeatureNames(restaurant.getId())
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return RestaurantDetailsResponse.builder()
                .id(restaurant.getId())
                .restaurantCode(restaurant.getRestaurantCode())
                .name(restaurant.getName())
                .email(restaurant.getEmail())
                .phoneNumber(restaurant.getPhoneNumber())
                .address(restaurant.getAddress())
                .settings(settings)
                .subscription(subscription)
                .enabledFeatures(enabledFeatures)
                .isActive(restaurant.getIsActive())
                .createdAt(restaurant.getCreatedAt())
                .build();
    }

    private RestaurantSettingsResponse mapToSettingsResponse(RestaurantSettings settings) {
        return RestaurantSettingsResponse.builder()
                .businessName(settings.getBusinessName())
                .businessType(settings.getBusinessType() != null ? settings.getBusinessType().name() : null)
                .currency(settings.getCurrency())
                .timezone(settings.getTimezone())
                .language(settings.getLanguage())
                .taxPercentage(settings.getTaxPercentage())
                .serviceChargePercentage(settings.getServiceChargePercentage())
                .autoAcceptOrders(settings.getAutoAcceptOrders())
                .allowOnlinePayments(settings.getAllowOnlinePayments())
                .allowCashPayments(settings.getAllowCashPayments())
                .minimumOrderAmount(settings.getMinimumOrderAmount())
                .deliveryRadiusKm(settings.getDeliveryRadiusKm())
                .averagePreparationTimeMinutes(settings.getAveragePreparationTimeMinutes())
                .logoUrl(settings.getLogoUrl())
                .bannerUrl(settings.getBannerUrl())
                .build();
    }

    private SubscriptionResponse mapToSubscriptionResponse(RestaurantSubscription subscription) {
        Long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getExpiryDate());

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .restaurantId(subscription.getRestaurantId())
                .plan(mapToPlanResponse(subscription.getPlan()))
                .status(subscription.getStatus().name())
                .startDate(subscription.getStartDate())
                .expiryDate(subscription.getExpiryDate())
                .daysRemaining(daysRemaining > 0 ? daysRemaining : 0L)
                .billingCycle(subscription.getBillingCycle() != null ? subscription.getBillingCycle().name() : null)
                .amountPaid(subscription.getAmountPaid())
                .paymentDate(subscription.getPaymentDate())
                .isAutoRenew(subscription.getIsAutoRenew())
                .build();
    }

    private SubscriptionPlanResponse mapToPlanResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .trialDays(plan.getTrialDays())
                .maxOrdersPerMonth(plan.getMaxOrdersPerMonth())
                .maxMenuItems(plan.getMaxMenuItems())
                .maxStaffUsers(plan.getMaxStaffUsers())
                .commissionPercentage(plan.getCommissionPercentage())
                .isActive(plan.getIsActive())
                .build();
    }

    private FeatureStatusResponse mapToFeatureStatusResponse(RestaurantFeature feature) {
        return FeatureStatusResponse.builder()
                .featureName(feature.getFeatureName().name())
                .isEnabled(feature.getIsEnabled())
                .enabledAt(feature.getEnabledAt())
                .enabledBy(feature.getEnabledBy())
                .build();
    }

    private BranchResponse mapToBranchResponse(RestaurantBranch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .contactEmail(branch.getContactEmail())
                .contactPhone(branch.getContactPhone())
                .address(branch.getAddress())
                .city(branch.getCity())
                .zipCode(branch.getZipCode())
                .state(branch.getState())
                .country(branch.getCountry())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .isMainBranch(branch.getIsMainBranch())
                .openingTime(branch.getOpeningTime())
                .closingTime(branch.getClosingTime())
                .isActive(branch.getIsActive())
                .createdAt(branch.getCreatedAt())
                .build();
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private boolean hasRole(UserPrincipal principal, String role) {
        return principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}
