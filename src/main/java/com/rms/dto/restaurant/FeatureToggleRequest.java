package com.rms.dto.restaurant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Feature Toggle Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Feature toggle request")
public class FeatureToggleRequest {

    @NotBlank(message = "Feature name is required")
    @Schema(description = "Feature name", example = "QR_ORDERING")
    private String featureName;

    @NotNull(message = "Enabled status is required")
    @Schema(description = "Enable or disable feature", example = "true")
    private Boolean isEnabled;

    @Schema(description = "Notes about the change")
    private String notes;
}
