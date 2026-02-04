package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesComparisonResponse {
    private SalesPeriodSummary period1;
    private SalesPeriodSummary period2;
    private BigDecimal revenueChange;
    private BigDecimal orderCountChange;
    private BigDecimal profitChange;
    private BigDecimal avgOrderValueChange;
}
