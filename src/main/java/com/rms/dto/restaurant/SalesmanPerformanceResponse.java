package com.rms.dto.restaurant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Salesman Performance Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Salesman performance metrics")
public class SalesmanPerformanceResponse {

    @Schema(description = "Salesman ID")
    private Long id;

    @Schema(description = "Salesman code")
    private String salesmanCode;

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Total restaurants onboarded")
    private Integer totalOnboarded;

    @Schema(description = "Active restaurants")
    private Integer totalActive;

    @Schema(description = "Conversion rate", example = "85.5")
    private BigDecimal conversionRate;

    @Schema(description = "Territory")
    private String territory;

    @Schema(description = "Commission percentage")
    private BigDecimal commissionPercentage;
}
