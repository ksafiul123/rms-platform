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
public class RevenueBreakdownResponse {
    private LocalDate date;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal netProfit;
    private Integer orderCount;
    private BigDecimal averageOrderValue;
}
