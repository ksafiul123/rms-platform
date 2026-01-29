package com.rms.dto;

//package com.rms.dto;

import com.rms.entity.CustomerPreference;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class PreferenceDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCustomerPreferenceRequest {

        private CustomerPreference.SpiceLevel spiceLevel;
        private CustomerPreference.SweetnessLevel sweetnessLevel;
        private CustomerPreference.SaltLevel saltLevel;
        private CustomerPreference.CookingPreference cookingPreference;
        private CustomerPreference.TemperaturePreference temperaturePreference;

        private Boolean isVegetarian;
        private Boolean isVegan;
        private Boolean isGlutenFree;
        private Boolean isDairyFree;
        private Boolean isNutFree;

        private List<@NotBlank @Size(max = 100) String> allergies;
        private List<@NotBlank @Size(max = 100) String> dislikes;

        @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
        private String specialInstructions;

        @Size(max = 20)
        private String portionPreference;

        private Boolean visibleToChefs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerPreferenceResponse {

        private Long id;
        private Long customerId;
        private CustomerPreference.SpiceLevel spiceLevel;
        private CustomerPreference.SweetnessLevel sweetnessLevel;
        private CustomerPreference.SaltLevel saltLevel;
        private CustomerPreference.CookingPreference cookingPreference;
        private CustomerPreference.TemperaturePreference temperaturePreference;
        private Boolean isVegetarian;
        private Boolean isVegan;
        private Boolean isGlutenFree;
        private Boolean isDairyFree;
        private Boolean isNutFree;
        private List<String> allergies;
        private List<String> dislikes;
        private String specialInstructions;
        private String portionPreference;
        private Boolean visibleToChefs;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddFavoriteRequest {

        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteMenuItemResponse {

        private Long id;
        private Long customerId;
        private Long menuItemId;
        private String menuItemName;
        private Long restaurantId;
        private String restaurantName;
        private String notes;
        private Integer orderCount;
        private LocalDateTime lastOrderedAt;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMenuItemPreferenceRequest {

        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        private CustomerPreference.SpiceLevel spiceLevel;
        private CustomerPreference.CookingPreference cookingPreference;

        @Size(max = 500, message = "Extra ingredients cannot exceed 500 characters")
        private String extraIngredients;

        @Size(max = 500, message = "Remove ingredients cannot exceed 500 characters")
        private String removeIngredients;

        @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
        private String specialInstructions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemPreferenceResponse {

        private Long id;
        private Long customerId;
        private Long menuItemId;
        private String menuItemName;
        private CustomerPreference.SpiceLevel spiceLevel;
        private CustomerPreference.CookingPreference cookingPreference;
        private String extraIngredients;
        private String removeIngredients;
        private String specialInstructions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChefViewCustomerPreferenceResponse {

        private Long customerId;
        private String customerName; // Anonymized or real based on privacy settings
        private CustomerPreference.SpiceLevel spiceLevel;
        private CustomerPreference.SweetnessLevel sweetnessLevel;
        private CustomerPreference.SaltLevel saltLevel;
        private CustomerPreference.CookingPreference cookingPreference;
        private CustomerPreference.TemperaturePreference temperaturePreference;
        private Boolean isVegetarian;
        private Boolean isVegan;
        private Boolean isGlutenFree;
        private Boolean isDairyFree;
        private Boolean isNutFree;
        private List<String> allergies;
        private List<String> dislikes;
        private String specialInstructions;
        private String portionPreference;

        // Menu item specific preferences (if order includes specific items)
        private MenuItemPreferenceResponse menuItemPreference;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderPreferencesSummary {

        private Long orderId;
        private String orderNumber;
        private Long customerId;
        private String customerName;

        // Global preferences
        private CustomerPreferenceResponse globalPreferences;

        // Item-specific preferences for this order
        private List<MenuItemPreferenceResponse> itemPreferences;

        // Highlights for kitchen
        private List<String> allergyWarnings;
        private List<String> dietaryRestrictions;
        private List<String> specialInstructions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferenceSummaryResponse {

        private Long customerId;
        private Boolean hasGlobalPreferences;
        private Boolean hasDietaryRestrictions;
        private Boolean hasAllergies;
        private Long favoriteCount;
        private Long itemPreferenceCount;
        private CustomerPreferenceResponse preferences;
    }
}
