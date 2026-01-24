package dev.safi.restaurant_management_system.dto.restaurant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Onboarding Status Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Restaurant onboarding status")
public class OnboardingStatusResponse {

    @Schema(description = "Restaurant ID")
    private Long restaurantId;

    @Schema(description = "Onboarding status", example = "SETUP_IN_PROGRESS")
    private String status;

    @Schema(description = "Steps completed", example = "3")
    private Integer stepCompleted;

    @Schema(description = "Total steps", example = "5")
    private Integer totalSteps;

    @Schema(description = "Completion percentage", example = "60")
    private Integer completionPercentage;

    @Schema(description = "Completed steps list")
    private List<String> completedSteps;

    @Schema(description = "Salesman name")
    private String salesmanName;

    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}
