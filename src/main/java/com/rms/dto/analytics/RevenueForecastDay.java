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
public class RevenueForecastDay {
    private LocalDate date;
    private BigDecimal projectedRevenue;
    private BigDecimal confidenceLevel;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
}
