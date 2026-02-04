package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal netProfit;
    private BigDecimal profitMargin;
    private List<RevenueBreakdownResponse> dailyBreakdown;
    private Map<String, BigDecimal> revenueByChannel;
    private Map<String, BigDecimal> revenueByPaymentMethod;
    private Map<String, BigDecimal> revenueByCategory;
}
