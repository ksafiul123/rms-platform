package dev.safi.restaurant_management_system.dto.restaurant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Subscription Plan Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscription plan details")
public class SubscriptionPlanResponse {

    @Schema(description = "Plan ID", example = "1")
    private Long id;

    @Schema(description = "Plan name", example = "STANDARD")
    private String name;

    @Schema(description = "Plan description")
    private String description;

    @Schema(description = "Monthly price", example = "2500.00")
    private BigDecimal monthlyPrice;

    @Schema(description = "Yearly price", example = "25000.00")
    private BigDecimal yearlyPrice;

    @Schema(description = "Trial days", example = "30")
    private Integer trialDays;

    @Schema(description = "Max orders per month", example = "1000")
    private Integer maxOrdersPerMonth;

    @Schema(description = "Max menu items", example = "100")
    private Integer maxMenuItems;

    @Schema(description = "Max staff users", example = "10")
    private Integer maxStaffUsers;

    @Schema(description = "Commission percentage", example = "5.00")
    private BigDecimal commissionPercentage;

    @Schema(description = "Active status")
    private Boolean isActive;
}
