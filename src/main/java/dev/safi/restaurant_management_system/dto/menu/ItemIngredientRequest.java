package dev.safi.restaurant_management_system.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Item Ingredient Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Item ingredient requirement")
public class ItemIngredientRequest {

    @NotNull(message = "Ingredient ID is required")
    @Schema(description = "Ingredient ID", example = "5")
    private Long ingredientId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001")
    @Schema(description = "Quantity needed", example = "0.250")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    @Schema(description = "Unit of measurement", example = "kg")
    private String unit;
}
