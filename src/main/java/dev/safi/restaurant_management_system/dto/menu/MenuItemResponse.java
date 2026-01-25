package dev.safi.restaurant_management_system.dto.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Menu Item Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Menu item details")
public class MenuItemResponse {

    private Long id;
    private Long restaurantId;
    private Long categoryId;
    private String categoryName;
    private String sku;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private BigDecimal finalPrice;
    private String imageUrl;
    private Integer preparationTimeMinutes;
    private String itemType;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isGlutenFree;
    private Boolean isSpicy;
    private Integer spiceLevel;
    private Integer calories;
    private String allergenInfo;
    private Boolean isAvailable;
    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean isBestSeller;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private Boolean availableForDineIn;
    private Boolean availableForTakeaway;
    private Boolean availableForDelivery;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Boolean isLowStock;
    private List<ItemVariantResponse> variants;
    private List<ModifierGroupResponse> modifierGroups;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
