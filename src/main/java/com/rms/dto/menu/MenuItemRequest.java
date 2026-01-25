package com.rms.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Menu Item Create/Update Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Menu item request")
public class MenuItemRequest {

    @NotNull(message = "Category is required")
    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 150)
    @Schema(description = "Item name", example = "Margherita Pizza")
    private String name;

    @Size(max = 1000)
    @Schema(description = "Item description")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Base price", example = "12.99")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0")
    @Schema(description = "Discounted price", example = "9.99")
    private BigDecimal discountedPrice;

    @Schema(description = "Image URL")
    private String imageUrl;

    @Min(1)
    @Max(300)
    @Schema(description = "Preparation time in minutes", example = "25")
    private Integer preparationTimeMinutes;

    @Schema(description = "Item type", example = "MAIN_COURSE")
    private String itemType;

    @Schema(description = "Is vegetarian", example = "true")
    private Boolean isVegetarian;

    @Schema(description = "Is vegan", example = "false")
    private Boolean isVegan;

    @Schema(description = "Is gluten free", example = "false")
    private Boolean isGlutenFree;

    @Schema(description = "Is spicy", example = "true")
    private Boolean isSpicy;

    @Min(0)
    @Max(5)
    @Schema(description = "Spice level (0-5)", example = "2")
    private Integer spiceLevel;

    @Schema(description = "Calories", example = "450")
    private Integer calories;

    @Schema(description = "Allergen information")
    private String allergenInfo;

    @Schema(description = "Available from time", example = "11:00")
    private LocalTime availableFrom;

    @Schema(description = "Available to time", example = "23:00")
    private LocalTime availableTo;

    @Schema(description = "Available for dine-in", example = "true")
    private Boolean availableForDineIn;

    @Schema(description = "Available for takeaway", example = "true")
    private Boolean availableForTakeaway;

    @Schema(description = "Available for delivery", example = "true")
    private Boolean availableForDelivery;

    @Schema(description = "Stock quantity (for limited items)")
    private Integer stockQuantity;

    @Schema(description = "Low stock threshold")
    private Integer lowStockThreshold;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Variants (sizes)")
    private List<ItemVariantRequest> variants;

    @Schema(description = "Modifier group IDs")
    private List<Long> modifierGroupIds;

    @Schema(description = "Ingredient requirements")
    private List<ItemIngredientRequest> ingredients;
}
