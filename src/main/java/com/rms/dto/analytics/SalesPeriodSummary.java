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
public class SalesPeriodSummary {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal netProfit;
    private BigDecimal averageOrderValue;
    private BigDecimal profitMargin;
}
