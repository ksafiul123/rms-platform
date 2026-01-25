package com.rms.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Modifier Group Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Modifier group request")
public class ModifierGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 100)
    @Schema(description = "Group name", example = "Choose Your Spice Level")
    private String name;

    @Schema(description = "Description")
    private String description;

    @NotBlank(message = "Selection type is required")
    @Schema(description = "Selection type", example = "SINGLE")
    private String selectionType;

    @Min(0)
    @Schema(description = "Minimum selections", example = "0")
    private Integer minSelections;

    @Min(1)
    @Schema(description = "Maximum selections", example = "1")
    private Integer maxSelections;

    @Schema(description = "Is required", example = "false")
    private Boolean isRequired;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @NotNull(message = "At least one option is required")
    @Size(min = 1)
    @Schema(description = "Modifier options")
    private List<ModifierOptionRequest> options;
}
