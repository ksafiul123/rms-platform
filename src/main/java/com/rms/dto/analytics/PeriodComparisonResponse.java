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
public class PeriodComparisonResponse {
    private String metric;
    private BigDecimal currentPeriod;
    private BigDecimal previousPeriod;
    private BigDecimal change;
    private BigDecimal percentageChange;
    private String trend; // UP, DOWN, STABLE
}
