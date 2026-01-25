package com.rms.dto.restaurant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Feature Status Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Feature status")
public class FeatureStatusResponse {

    @Schema(description = "Feature name", example = "QR_ORDERING")
    private String featureName;

    @Schema(description = "Is enabled", example = "true")
    private Boolean isEnabled;

    @Schema(description = "Enabled at")
    private LocalDateTime enabledAt;

    @Schema(description = "Enabled by user ID")
    private Long enabledBy;
}
