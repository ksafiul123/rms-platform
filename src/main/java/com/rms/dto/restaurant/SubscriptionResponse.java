package com.rms.dto.restaurant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Restaurant Subscription Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Restaurant subscription details")
public class SubscriptionResponse {

    @Schema(description = "Subscription ID", example = "1")
    private Long id;

    @Schema(description = "Restaurant ID", example = "5")
    private Long restaurantId;

    @Schema(description = "Plan details")
    private SubscriptionPlanResponse plan;

    @Schema(description = "Subscription status", example = "ACTIVE")
    private String status;

    @Schema(description = "Start date")
    private LocalDateTime startDate;

    @Schema(description = "Expiry date")
    private LocalDateTime expiryDate;

    @Schema(description = "Days remaining", example = "25")
    private Long daysRemaining;

    @Schema(description = "Billing cycle", example = "MONTHLY")
    private String billingCycle;

    @Schema(description = "Amount paid", example = "2500.00")
    private BigDecimal amountPaid;

    @Schema(description = "Payment date")
    private LocalDateTime paymentDate;

    @Schema(description = "Auto renew enabled")
    private Boolean isAutoRenew;
}
