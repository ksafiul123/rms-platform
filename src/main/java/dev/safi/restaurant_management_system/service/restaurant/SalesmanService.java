package dev.safi.restaurant_management_system.service.restaurant;

//package com.rms.service.restaurant;

import dev.safi.restaurant_management_system.dto.auth.ApiResponse;
import dev.safi.restaurant_management_system.dto.restaurant.*;
import dev.safi.restaurant_management_system.entity.*;
import dev.safi.restaurant_management_system.enums.BillingCycle;
import dev.safi.restaurant_management_system.enums.OnboardingStatus;
import dev.safi.restaurant_management_system.enums.SubscriptionStatus;
import dev.safi.restaurant_management_system.exception.BadRequestException;
import dev.safi.restaurant_management_system.exception.ResourceNotFoundException;
import dev.safi.restaurant_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Salesman Service - Manages salesman operations and restaurant onboarding
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesmanService {

    private final SalesmanRepository salesmanRepository;
    private final RestaurantOnboardingRepository onboardingRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Register a new salesman
     */
    @Transactional
    public ApiResponse<SalesmanPerformanceResponse> registerSalesman(Long userId) {
        if (salesmanRepository.existsByUserId(userId)) {
            throw new BadRequestException("User is already registered as salesman");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String salesmanCode = generateSalesmanCode();

        Salesman salesman = Salesman.builder()
                .salesmanCode(salesmanCode)
                .userId(userId)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .commissionPercentage(BigDecimal.valueOf(5.0))
                .totalOnboarded(0)
                .totalActive(0)
                .isActive(true)
                .joinedDate(LocalDateTime.now())
                .build();

        salesman = salesmanRepository.save(salesman);

        log.info("Salesman registered: {} (Code: {})", user.getFullName(), salesmanCode);

        return ApiResponse.success(
                "Salesman registered successfully",
                mapToPerformanceResponse(salesman)
        );
    }

    /**
     * Initiate restaurant onboarding
     */
    @Transactional
    public ApiResponse<OnboardingStatusResponse> initiateOnboarding(Long restaurantId, Long salesmanId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Salesman salesman = salesmanRepository.findById(salesmanId)
                .orElseThrow(() -> new ResourceNotFoundException("Salesman not found"));

        // Check if onboarding already exists
        if (onboardingRepository.findByRestaurantId(restaurantId).isPresent()) {
            throw new BadRequestException("Onboarding already initiated for this restaurant");
        }

        RestaurantOnboarding onboarding = RestaurantOnboarding.builder()
                .restaurantId(restaurantId)
                .salesmanId(salesmanId)
                .status(OnboardingStatus.INITIATED)
                .stepCompleted(0)
                .totalSteps(5)
                .completedSteps("")
                .build();

        onboarding = onboardingRepository.save(onboarding);

        // Increment salesman's total onboarded count
        salesmanRepository.incrementTotalOnboarded(salesmanId);

        log.info("Onboarding initiated for restaurant: {} by salesman: {}",
                restaurant.getName(), salesman.getFullName());

        return ApiResponse.success(
                "Onboarding initiated successfully",
                mapToOnboardingStatusResponse(onboarding, salesman)
        );
    }

    /**
     * Update onboarding status
     */
    @Transactional
    public ApiResponse<OnboardingStatusResponse> updateOnboardingStatus(
            Long restaurantId,
            OnboardingStatus status,
            String notes) {

        RestaurantOnboarding onboarding = onboardingRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding record not found"));

        onboarding.setStatus(status);
        onboarding.setNotes(notes);

        // Update step completion based on status
        switch (status) {
            case INITIATED:
                onboarding.setStepCompleted(0);
                break;
            case DOCUMENTS_PENDING:
                onboarding.setStepCompleted(1);
                break;
            case DOCUMENTS_VERIFIED:
                onboarding.setStepCompleted(2);
                break;
            case SETUP_IN_PROGRESS:
                onboarding.setStepCompleted(3);
                break;
            case TRAINING_SCHEDULED:
                onboarding.setStepCompleted(4);
                break;
            case COMPLETED:
                onboarding.setStepCompleted(5);
                onboarding.setOnboardedAt(LocalDateTime.now());

                // Increment salesman's active count
                if (onboarding.getSalesmanId() != null) {
                    salesmanRepository.incrementTotalActive(onboarding.getSalesmanId());
                }
                break;
            case REJECTED:
                // Decrement salesman's counts if rejected
                if (onboarding.getSalesmanId() != null) {
                    // Don't decrement onboarded as attempt was made
                }
                break;
        }

        onboarding = onboardingRepository.save(onboarding);

        Salesman salesman = onboarding.getSalesmanId() != null
                ? salesmanRepository.findById(onboarding.getSalesmanId()).orElse(null)
                : null;

        log.info("Onboarding status updated to {} for restaurant ID: {}", status, restaurantId);

        return ApiResponse.success(
                "Onboarding status updated successfully",
                mapToOnboardingStatusResponse(onboarding, salesman)
        );
    }

    /**
     * Get onboarding status
     */
    @Transactional(readOnly = true)
    public ApiResponse<OnboardingStatusResponse> getOnboardingStatus(Long restaurantId) {
        RestaurantOnboarding onboarding = onboardingRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding record not found"));

        Salesman salesman = onboarding.getSalesmanId() != null
                ? salesmanRepository.findById(onboarding.getSalesmanId()).orElse(null)
                : null;

        return ApiResponse.success(
                "Onboarding status fetched successfully",
                mapToOnboardingStatusResponse(onboarding, salesman)
        );
    }

    /**
     * Get salesman performance metrics
     */
    @Transactional(readOnly = true)
    public ApiResponse<SalesmanPerformanceResponse> getSalesmanPerformance(Long salesmanId) {
        Salesman salesman = salesmanRepository.findById(salesmanId)
                .orElseThrow(() -> new ResourceNotFoundException("Salesman not found"));

        return ApiResponse.success(
                "Salesman performance fetched successfully",
                mapToPerformanceResponse(salesman)
        );
    }

    /**
     * Get all restaurants onboarded by salesman
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<RestaurantListResponse>> getSalesmanRestaurants(Long salesmanId) {
        List<RestaurantOnboarding> onboardings = onboardingRepository.findBySalesmanId(salesmanId);

        List<RestaurantListResponse> restaurants = onboardings.stream()
                .map(onboarding -> {
                    Restaurant restaurant = restaurantRepository.findById(onboarding.getRestaurantId())
                            .orElse(null);

                    if (restaurant == null) {
                        return null;
                    }

                    RestaurantSubscription subscription = subscriptionRepository
                            .findLatestByRestaurantId(restaurant.getId())
                            .orElse(null);

                    return mapToRestaurantListResponse(restaurant, subscription);
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        return ApiResponse.success("Restaurants fetched successfully", restaurants);
    }

    /**
     * Assign subscription plan to restaurant
     */
    @Transactional
    public ApiResponse<SubscriptionResponse> assignSubscriptionPlan(
            Long restaurantId,
            String planName,
            String billingCycle) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        SubscriptionPlan plan = planRepository.findByNameAndIsActiveTrue(planName)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        BillingCycle cycle = BillingCycle.valueOf(billingCycle);

        // Calculate subscription duration and amount
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime expiryDate;
        BigDecimal amount;

        switch (cycle) {
            case MONTHLY:
                expiryDate = startDate.plusMonths(1);
                amount = plan.getMonthlyPrice();
                break;
            case QUARTERLY:
                expiryDate = startDate.plusMonths(3);
                amount = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(3));
                break;
            case YEARLY:
                expiryDate = startDate.plusYears(1);
                amount = plan.getYearlyPrice() != null ? plan.getYearlyPrice() : plan.getMonthlyPrice().multiply(BigDecimal.valueOf(12));
                break;
            case LIFETIME:
                expiryDate = startDate.plusYears(100); // Effectively lifetime
                amount = plan.getYearlyPrice().multiply(BigDecimal.valueOf(10));
                break;
            default:
                throw new BadRequestException("Invalid billing cycle");
        }

        // Create subscription
        RestaurantSubscription subscription = RestaurantSubscription.builder()
                .restaurantId(restaurantId)
                .plan(plan)
                .status(SubscriptionStatus.TRIAL)
                .startDate(startDate)
                .expiryDate(expiryDate)
                .billingCycle(cycle)
                .amountPaid(BigDecimal.ZERO) // Trial doesn't require payment
                .isAutoRenew(false)
                .build();

        subscription = subscriptionRepository.save(subscription);

        // Update restaurant subscription status
        restaurant.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        restaurant.setSubscriptionExpiry(expiryDate);
        restaurantRepository.save(restaurant);

        log.info("Subscription plan {} assigned to restaurant: {}", planName, restaurant.getName());

        return ApiResponse.success(
                "Subscription assigned successfully",
                mapToSubscriptionResponse(subscription)
        );
    }

    /**
     * Get all available subscription plans
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<SubscriptionPlanResponse>> getAllPlans() {
        List<SubscriptionPlan> plans = planRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();

        List<SubscriptionPlanResponse> responses = plans.stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Subscription plans fetched successfully", responses);
    }

    // ==================== Helper Methods ====================

    private String generateSalesmanCode() {
        String code;
        do {
            code = "SM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (salesmanRepository.existsBySalesmanCode(code));
        return code;
    }

    private SalesmanPerformanceResponse mapToPerformanceResponse(Salesman salesman) {
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (salesman.getTotalOnboarded() > 0) {
            conversionRate = BigDecimal.valueOf(salesman.getTotalActive())
                    .divide(BigDecimal.valueOf(salesman.getTotalOnboarded()), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return SalesmanPerformanceResponse.builder()
                .id(salesman.getId())
                .salesmanCode(salesman.getSalesmanCode())
                .fullName(salesman.getFullName())
                .totalOnboarded(salesman.getTotalOnboarded())
                .totalActive(salesman.getTotalActive())
                .conversionRate(conversionRate)
                .territory(salesman.getTerritory())
                .commissionPercentage(salesman.getCommissionPercentage())
                .build();
    }

    private OnboardingStatusResponse mapToOnboardingStatusResponse(
            RestaurantOnboarding onboarding,
            Salesman salesman) {

        int completionPercentage = (onboarding.getStepCompleted() * 100) / onboarding.getTotalSteps();

        List<String> completedSteps = onboarding.getCompletedSteps() != null
                ? List.of(onboarding.getCompletedSteps().split(","))
                : List.of();

        return OnboardingStatusResponse.builder()
                .restaurantId(onboarding.getRestaurantId())
                .status(onboarding.getStatus().name())
                .stepCompleted(onboarding.getStepCompleted())
                .totalSteps(onboarding.getTotalSteps())
                .completionPercentage(completionPercentage)
                .completedSteps(completedSteps)
                .salesmanName(salesman != null ? salesman.getFullName() : null)
                .notes(onboarding.getNotes())
                .createdAt(onboarding.getCreatedAt())
                .build();
    }

    private RestaurantListResponse mapToRestaurantListResponse(
            Restaurant restaurant,
            RestaurantSubscription subscription) {

        Integer daysRemaining = null;
        if (subscription != null && subscription.getExpiryDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDateTime.now(), subscription.getExpiryDate());
            daysRemaining = (int) Math.max(0, days);
        }

        return RestaurantListResponse.builder()
                .id(restaurant.getId())
                .restaurantCode(restaurant.getRestaurantCode())
                .name(restaurant.getName())
                .email(restaurant.getEmail())
                .phoneNumber(restaurant.getPhoneNumber())
                .subscriptionStatus(restaurant.getSubscriptionStatus().name())
                .subscriptionExpiry(restaurant.getSubscriptionExpiry())
                .daysRemaining(daysRemaining)
                .isActive(restaurant.getIsActive())
                .createdAt(restaurant.getCreatedAt())
                .build();
    }

    private SubscriptionResponse mapToSubscriptionResponse(RestaurantSubscription subscription) {
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now(), subscription.getExpiryDate());

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .restaurantId(subscription.getRestaurantId())
                .plan(mapToPlanResponse(subscription.getPlan()))
                .status(subscription.getStatus().name())
                .startDate(subscription.getStartDate())
                .expiryDate(subscription.getExpiryDate())
                .daysRemaining(Math.max(0, daysRemaining))
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
}
