package com.rms.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Item Variant Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Item variant request")
public class ItemVariantRequest {

    @NotBlank(message = "Variant name is required")
    @Schema(description = "Variant name", example = "Large")
    private String name;

    @NotNull(message = "Price adjustment is required")
    @Schema(description = "Price adjustment", example = "3.00")
    private BigDecimal priceAdjustment;

    @Schema(description = "Is default variant", example = "false")
    private Boolean isDefault;

    @Schema(description = "Display order", example = "2")
    private Integer displayOrder;
}
