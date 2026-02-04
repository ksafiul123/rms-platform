package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetricsResponse {
    private LocalDate date;

    // Order metrics
    private Integer totalOrders;
    private BigDecimal orderFulfillmentRate;
    private BigDecimal avgPreparationTime;
    private BigDecimal avgDeliveryTime;
    private BigDecimal onTimeDeliveryRate;

    // Customer metrics
    private Integer totalCustomers;
    private BigDecimal customerSatisfactionScore;
    private BigDecimal customerRetentionRate;
    private BigDecimal repeatOrderRate;

    // Financial metrics
    private BigDecimal totalRevenue;
    private BigDecimal grossProfit;
    private BigDecimal netProfit;
    private BigDecimal profitMargin;
    private BigDecimal averageOrderValue;

    // Operational metrics
    private BigDecimal tableTurnoverRate;
    private BigDecimal inventoryTurnoverRate;
    private BigDecimal labourCostPercentage;
    private BigDecimal foodCostPercentage;
}
