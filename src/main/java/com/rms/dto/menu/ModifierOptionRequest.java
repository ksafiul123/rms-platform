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
 * Modifier Option Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Modifier option request")
public class ModifierOptionRequest {

    @NotBlank(message = "Option name is required")
    @Schema(description = "Option name", example = "Mild")
    private String name;

    @Schema(description = "Description")
    private String description;

    @NotNull(message = "Price adjustment is required")
    @Schema(description = "Price adjustment", example = "0.00")
    private BigDecimal priceAdjustment;

    @Schema(description = "Is default option", example = "true")
    private Boolean isDefault;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;
}
