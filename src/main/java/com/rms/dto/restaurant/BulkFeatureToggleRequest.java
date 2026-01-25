package com.rms.dto.restaurant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Bulk Feature Toggle Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bulk feature toggle request")
public class BulkFeatureToggleRequest {

    @NotNull(message = "Features are required")
    @Size(min = 1, message = "At least one feature is required")
    @Schema(description = "Map of feature names to enabled status")
    private Map<String, Boolean> features;

    @Schema(description = "Notes about the bulk change")
    private String notes;
}
